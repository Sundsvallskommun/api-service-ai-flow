package se.sundsvall.ai.flow.service;

import static java.util.Optional.ofNullable;
import static org.zalando.problem.Status.NOT_FOUND;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import se.sundsvall.ai.flow.integration.intric.IntricService;
import se.sundsvall.ai.flow.integration.templating.TemplatingIntegration;
import se.sundsvall.ai.flow.model.flowdefinition.Flow;
import se.sundsvall.ai.flow.model.flowdefinition.FlowInput;
import se.sundsvall.ai.flow.model.session.Input;
import se.sundsvall.ai.flow.model.session.Session;
import se.sundsvall.ai.flow.model.session.StepExecution;
import se.sundsvall.dept44.requestid.RequestId;

@Service
public class SessionService {

	private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();

	private final Executor executor;
	private final IntricService intricService;
	private final TemplatingIntegration templatingIntegration;

	SessionService(final Executor executor, final IntricService intricService, final TemplatingIntegration templatingIntegration) {
		this.executor = executor;
		this.intricService = intricService;
		this.templatingIntegration = templatingIntegration;
	}

	public Session createSession(final Flow flow) {
		var session = new Session(flow);
		sessions.put(session.getId(), session);
		return session;
	}

	public Session getSession(final UUID sessionId) {
		return ofNullable(sessions.get(sessionId))
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "No session exists with id " + sessionId));
	}

	Collection<Session> getAllSessions() {
		return sessions.values();
	}

	public void executeSession(final UUID sessionId) {
		var session = getSession(sessionId);
		var flow = session.getFlow();

		// Make sure that all required inputs have values
		var unsetRequiredInputs = flow.getFlowInputs().stream()
			.filter(FlowInput::isRequired)
			.map(FlowInput::getId)
			.filter(inputId -> session.getInput().getOrDefault(inputId, List.of()).isEmpty())
			.toList();
		if (!unsetRequiredInputs.isEmpty()) {
			throw Problem.valueOf(Status.BAD_REQUEST, "Unable to execute session %s as the following required inputs are unset or empty: %s".formatted(sessionId, unsetRequiredInputs));
		}

		executor.executeSession(session, RequestId.get());
	}

	public void executeStep(final UUID sessionId, final String stepId, final String input, final boolean runRequiredSteps) {
		var session = getSession(sessionId);
		var stepExecution = session.getStepExecution(stepId);

		executor.executeStep(stepExecution, input, runRequiredSteps);
	}

	public void deleteSession(final UUID sessionId) {
		var session = getSession(sessionId);

		// Extract the id:s of the files uploaded in the session
		var uploadedFileIds = Stream.concat(session.getInput().values().stream(), session.getRedirectedOutputInput().values().stream())
			.flatMap(Collection::stream)
			.map(Input::getIntricFileId)
			.flatMap(Stream::ofNullable)
			.toList();
		// Delete the files
		intricService.deleteFiles(uploadedFileIds);
		// Remove the session
		sessions.remove(sessionId);
	}

	public Session addInput(final UUID sessionId, final String inputId, final String value) {
		var session = getSession(sessionId);
		session.addSimpleInput(inputId, value);
		return session;
	}

	public Session addInput(final UUID sessionId, final String inputId, final MultipartFile inputMultipartFile) {
		var session = getSession(sessionId);
		session.addFileInput(inputId, inputMultipartFile);
		return session;
	}

	public Session clearInput(final UUID sessionId, final String inputId) {
		var session = getSession(sessionId);
		session.clearInput(inputId);
		return session;
	}

	public String renderSession(final UUID sessionId, final String templateId, final String municipalityId) {
		var session = getSession(sessionId);

		return templatingIntegration.renderSession(session, templateId, municipalityId);
	}

	public StepExecution getStepExecution(final UUID sessionId, final String stepId) {
		var session = getSession(sessionId);

		return session.getStepExecution(stepId);
	}
}
