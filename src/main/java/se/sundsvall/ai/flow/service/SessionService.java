package se.sundsvall.ai.flow.service;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import se.sundsvall.ai.flow.integration.templating.TemplatingIntegration;
import se.sundsvall.ai.flow.model.Session;
import se.sundsvall.ai.flow.model.flow.FlowInputRef;
import se.sundsvall.ai.flow.model.flow.RedirectedOutput;
import se.sundsvall.ai.flow.model.flow.Step;
import se.sundsvall.ai.flow.service.flow.StepExecution;

@Service
public class SessionService {

	private static final Logger LOG = LoggerFactory.getLogger(SessionService.class);

	private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();

	private final FlowRegistry flowRegistry;
	private final TemplatingIntegration templatingIntegration;

	SessionService(final FlowRegistry flowRegistry, final TemplatingIntegration templatingIntegration) {
		this.flowRegistry = flowRegistry;
		this.templatingIntegration = templatingIntegration;
	}

	public Session createSession(final String flowId) {
		var flow = flowRegistry.getFlow(flowId);
		var session = new Session().withFlow(flow);

		// Store the session
		sessions.put(session.getId(), session);

		return session;
	}

	public Session getSession(final UUID sessionId) {
		return ofNullable(sessions.get(sessionId))
			.orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND, "No session exists with id " + sessionId));
	}

	public StepExecution getStepExecution(final UUID sessionId, final String stepId) {
		var session = getSession(sessionId);

		return session.getStepExecution(stepId);
	}

	public Session addInput(final UUID sessionId, final String inputId, final String value) {
		var session = getSession(sessionId);
		session.addInput(inputId, value);
		return session;
	}

	public Session replaceInput(final UUID sessionId, final String inputId, final String value) {
		var session = getSession(sessionId);
		session.replaceInput(inputId, value);
		return session;
	}

	public StepExecution createStepExecution(final UUID sessionId, final String stepId) {
		var session = getSession(sessionId);
		var flow = session.getFlow();
		var step = getStep(sessionId, stepId);

		// Validate the session input refs
		for (var stepInput : step.getInputs()) {
			if (stepInput instanceof FlowInputRef flowInputRef) {
				// Make sure that the step input is set in the session
				var sessionInput = session.getInput();
				if (sessionInput != null && !sessionInput.containsKey(flowInputRef.getInput())) {
					throw Problem.valueOf(Status.BAD_REQUEST, "Required input '%s' is unset for step '%s' in flow '%s' for session %s".formatted(flowInputRef.getInput(), step.getName(), flow.getName(), sessionId));
				}
			}
		}

		// Validate redirected output inputs
		var requiredStepExecutions = new ArrayList<StepExecution>();
		for (var stepInput : step.getInputs()) {
			if (stepInput instanceof RedirectedOutput redirectedOutput) {
				// Make sure required step(s) have been executed before this one
				var sourceStepId = redirectedOutput.getStep();

				if (session.getStepExecutions() == null || !session.getStepExecutions().containsKey(sourceStepId) || isBlank(session.getStepExecutions().get(sourceStepId).getOutput())) {
					LOG.info("Missing redirected output from step '{}' for step '{}' in flow '{}' for session {}", sourceStepId, stepId, flow.getName(), sessionId);

					requiredStepExecutions.add(createStepExecution(sessionId, sourceStepId));
				}
			}
		}

		// Mark the session as running
		session.setState(Session.State.RUNNING);

		LOG.info("Created step execution for step '{}' from flow '{}' for session {}", step.getName(), flow.getName(), session.getId());

		var stepExecution = new StepExecution(sessionId, step, requiredStepExecutions);
		session.addStepExecution(step.getId(), stepExecution);
		return stepExecution;
	}

	public String renderSession(final UUID sessionId, final String templateId, final String municipalityId) {
		var session = getSession(sessionId);

		return templatingIntegration.renderSession(session, templateId, municipalityId);
	}

	Step getStep(final UUID sessionId, final String stepId) {
		var session = getSession(sessionId);
		var flow = session.getFlow();

		return ofNullable(flow.getStep(stepId))
			.orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND, "No step with '%s' exists in flow '%s' for session %s".formatted(stepId, flow.getName(), sessionId)));
	}
}
