package se.sundsvall.ai.flow.service.execution;

import java.util.List;
import org.springframework.stereotype.Component;
import se.sundsvall.ai.flow.model.session.Session;

@Component
public class SessionOrchestrator {

	private final FileUploadManager fileUploadManager;
	private final StepRunner stepRunner;

	public SessionOrchestrator(final FileUploadManager fileUploadManager,
		final StepRunner stepRunner) {
		this.fileUploadManager = fileUploadManager;
		this.stepRunner = stepRunner;
	}

	public void runSession(final String municipalityId, final Session session) {
		final var flow = session.getFlow();

		// Upload all inputs
		fileUploadManager.uploadMissing(municipalityId, session);

		// Mark running
		session.setState(Session.State.RUNNING);

		// Execute steps in order
		for (final var step : flow.getSteps()) {
			final var stepExecution = session.getStepExecution(step.getId());

			// Build a minimal StepRunContext for the stepRunner to use; StepRunner will re-resolve inputs
			final var context = new StepRunContext(municipalityId, session, stepExecution, List.of(), List.of(), "", null, true);
			final var result = stepRunner.runStep(context); // StepRunner performs state updates

			// If a step failed, mark the session as ERROR and stop executing remaining steps
			if (!result.success()) {
				session.setState(Session.State.ERROR);
				return;
			}
		}

		session.setState(Session.State.FINISHED);
	}
}
