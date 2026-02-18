package se.sundsvall.ai.flow.service;

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
import se.sundsvall.ai.flow.integration.eneo.EneoService;
import se.sundsvall.ai.flow.integration.templating.TemplatingIntegration;
import se.sundsvall.ai.flow.model.flowdefinition.Flow;
import se.sundsvall.ai.flow.model.flowdefinition.FlowInput;
import se.sundsvall.ai.flow.model.session.FileInputValue;
import se.sundsvall.ai.flow.model.session.Input;
import se.sundsvall.ai.flow.model.session.Session;
import se.sundsvall.ai.flow.model.session.StepExecution;
import se.sundsvall.ai.flow.util.DocumentUtil;
import se.sundsvall.dept44.requestid.RequestId;

import static java.util.Optional.ofNullable;
import static org.zalando.problem.Status.NOT_FOUND;

@Service
public class SessionService {

	private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();

	private final Executor executor;
	private final EneoService eneoService;
	private final TemplatingIntegration templatingIntegration;

	SessionService(final Executor executor, final EneoService eneoService, final TemplatingIntegration templatingIntegration) {
		this.executor = executor;
		this.eneoService = eneoService;
		this.templatingIntegration = templatingIntegration;
	}

	public Session createSession(final String municipalityId, final Flow flow) {
		final var session = new Session(municipalityId, flow);
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

	public void executeSession(final String municipalityId, final UUID sessionId) {
		final var session = getSession(sessionId);
		final var flow = session.getFlow();

		// Make sure that all required inputs have values
		final var unsetRequiredInputs = flow.getFlowInputs().stream()
			.filter(FlowInput::isRequired)
			.map(FlowInput::getId)
			.filter(inputId -> session.getInput().getOrDefault(inputId, List.of()).isEmpty())
			.toList();
		if (!unsetRequiredInputs.isEmpty()) {
			throw Problem.valueOf(Status.BAD_REQUEST, "Unable to execute session %s as the following required inputs are unset or empty: %s".formatted(sessionId, unsetRequiredInputs));
		}

		executor.executeSession(municipalityId, session, RequestId.get());
	}

	public void executeStep(final String municipalityId, final UUID sessionId, final String stepId, final String input, final boolean runRequiredSteps) {
		final var session = getSession(sessionId);
		final var stepExecution = session.getStepExecution(stepId);

		executor.executeStep(municipalityId, stepExecution, input, runRequiredSteps);
	}

	public void deleteSession(final String municipalityId, final UUID sessionId) {
		final var session = getSession(sessionId);

		// Extract the id:s of the files uploaded in the session
		final var uploadedFileIds = Stream.concat(session.getInput().values().stream(), session.getRedirectedOutputInput().values().stream())
			.flatMap(Collection::stream)
			.map(Input::getEneoFileId)
			.flatMap(Stream::ofNullable)
			.toList();
		// Delete the files
		eneoService.deleteFiles(municipalityId, uploadedFileIds);
		// Remove the session
		sessions.remove(sessionId);
	}

	public Session addInput(final UUID sessionId, final String inputId, final String value) {
		final var session = getSession(sessionId);
		session.addSimpleInput(inputId, value);
		return session;
	}

	public Session addInput(final UUID sessionId, final String inputId, final MultipartFile inputMultipartFile) {
		final var session = getSession(sessionId);

		// Only removes images from PDF and DOCX files, other file types are returned as-is
		var documentBytes = DocumentUtil.removeImages(inputMultipartFile);

		final var value = new FileInputValue(
			inputMultipartFile.getOriginalFilename(),
			documentBytes,
			inputMultipartFile.getContentType());
		session.addInput(inputId, value);
		return session;
	}

	public Session clearInput(final UUID sessionId, final String inputId) {
		final var session = getSession(sessionId);
		session.clearInput(inputId);
		return session;
	}

	public String renderSession(final UUID sessionId, final String templateId, final String municipalityId) {
		final var session = getSession(sessionId);

		return templatingIntegration.renderSession(session, templateId, municipalityId);
	}

	public StepExecution getStepExecution(final UUID sessionId, final String stepId) {
		final var session = getSession(sessionId);

		return session.getStepExecution(stepId);
	}
}
