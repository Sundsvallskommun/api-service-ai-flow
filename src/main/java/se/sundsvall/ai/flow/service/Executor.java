package se.sundsvall.ai.flow.service;

import java.util.List;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import se.sundsvall.ai.flow.model.session.Session;
import se.sundsvall.ai.flow.model.session.StepExecution;
import se.sundsvall.ai.flow.service.execution.SessionOrchestrator;
import se.sundsvall.ai.flow.service.execution.StepRunContext;
import se.sundsvall.ai.flow.service.execution.StepRunner;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.requestid.RequestId;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Component
public class Executor {

	private final StepRunner stepRunner;
	private final SessionOrchestrator sessionOrchestrator;

	public Executor(final StepRunner stepRunner,
		final SessionOrchestrator sessionOrchestrator) {
		this.stepRunner = stepRunner;
		this.sessionOrchestrator = sessionOrchestrator;
	}

	@Async
	public void executeSession(final String municipalityId, final Session session, final String requestId) {
		RequestId.init(requestId);
		try {
			sessionOrchestrator.runSession(municipalityId, session);
		} finally {
			RequestId.reset();
		}
	}

	@Async
	public void executeStep(final String municipalityId, final StepExecution stepExecution, final String input, final boolean runRequiredSteps) {
		final var session = stepExecution.getSession();

		// The session must either be running or finished before allowing any individual steps to be executed individually
		if (session.getState() != Session.State.RUNNING && session.getState() != Session.State.FINISHED) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Unable to run step '%s' in flow '%s' for session %s since the session has never been run yet".formatted(stepExecution.getStep().getId(), session.getFlow().getName(), session.getId()));
		}

		// Make sure the step isn't already running
		if (stepExecution.isRunning()) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Unable to run already running step '%s' in flow '%s' for session %s".formatted(stepExecution.getStep().getId(), session.getFlow().getName(), session.getId()));
		}

		executeStepInternal(municipalityId, stepExecution, input, runRequiredSteps);
	}

	void executeStepInternal(final String municipalityId, final StepExecution stepExecution, final String input, final boolean runRequiredSteps) {
		// Build context and delegate to StepRunner (StepRunner will re-resolve inputs via InputPreparation)
		final var context = new StepRunContext(municipalityId, stepExecution.getSession(), stepExecution, List.of(), List.of(), "", input, runRequiredSteps);
		stepRunner.runStep(context);
	}
}
