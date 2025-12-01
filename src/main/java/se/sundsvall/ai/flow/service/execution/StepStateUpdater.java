package se.sundsvall.ai.flow.service.execution;

import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import se.sundsvall.ai.flow.model.session.StepExecution;

/** Centralizes state transitions and timestamp updates for step executions. */
@Component
public class StepStateUpdater {

	public void markRunning(final StepExecution stepExecution) {
		stepExecution.setState(StepExecution.State.RUNNING);
	}

	public void markFinished(final StepExecution stepExecution, final String output, final UUID runId, final UUID sessionId) {
		Optional.ofNullable(runId).ifPresent(stepExecution::setEneoRunId);
		Optional.ofNullable(sessionId).ifPresent(stepExecution::setEneoSessionId);

		stepExecution.setOutput(output);
		stepExecution.setState(StepExecution.State.DONE);
	}

	public void markFailed(final StepExecution stepExecution, final String message) {
		stepExecution.setErrorMessage(message);
		stepExecution.setState(StepExecution.State.ERROR);
	}
}
