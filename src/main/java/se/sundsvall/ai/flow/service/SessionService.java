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
import se.sundsvall.ai.flow.integration.intric.IntricIntegration;
import se.sundsvall.ai.flow.integration.templating.TemplatingIntegration;
import se.sundsvall.ai.flow.model.flowdefinition.Flow;
import se.sundsvall.ai.flow.model.flowdefinition.FlowInput;
import se.sundsvall.ai.flow.model.flowdefinition.Step;
import se.sundsvall.ai.flow.model.session.Input;
import se.sundsvall.ai.flow.model.session.Session;

@Service
public class SessionService {

	private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();

	private final Executor executor;
	private final IntricIntegration intricIntegration;
	private final TemplatingIntegration templatingIntegration;

	SessionService(final Executor executor, final IntricIntegration intricIntegration, final TemplatingIntegration templatingIntegration) {
		this.executor = executor;
		this.intricIntegration = intricIntegration;
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

		executor.executeSession(session);
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
		intricIntegration.deleteFiles(uploadedFileIds);
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

	public Step getStep(final Session session, final String stepId) {
		var flow = session.getFlow();

		return flow.getSteps().stream()
			.filter(step -> stepId.equals(step.getId()))
			.findFirst()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "No step with '%s' exists in flow '%s' for session %s".formatted(stepId, flow.getName(), session.getId())));
	}
}
