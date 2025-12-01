package se.sundsvall.ai.flow.service.execution;

import java.util.UUID;

public record StepExecutionResult(boolean success, String output, UUID runId, UUID sessionId, String errorMessage) {

	public static StepExecutionResult success(final String output, final UUID runId, final UUID sessionId) {
		return new StepExecutionResult(true, output, runId, sessionId, null);
	}

	public static StepExecutionResult failure(final String message) {
		return new StepExecutionResult(false, null, null, null, message);
	}
}
