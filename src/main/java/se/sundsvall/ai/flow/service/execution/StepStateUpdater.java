package se.sundsvall.ai.flow.service.execution;

import java.util.UUID;
import org.springframework.stereotype.Component;
import se.sundsvall.ai.flow.model.session.StepExecution;

/** Centralizes state transitions and timestamp updates for step executions. */
@Component
public class StepStateUpdater {

	public void markRunning(final StepExecution exec) {
		exec.setState(StepExecution.State.RUNNING);
	}

	public void markFinished(final StepExecution exec, final String output, final UUID runId, final UUID sessionId) {
		if (runId != null) {
			exec.setIntricRunId(runId);
		}
		if (sessionId != null) {
			exec.setIntricSessionId(sessionId);
		}
		exec.setOutput(output);
		exec.setState(StepExecution.State.DONE);
	}

	public void markFailed(final StepExecution exec, final String message) {
		exec.setErrorMessage(message);
		exec.setState(StepExecution.State.ERROR);
	}
}
