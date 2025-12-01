package se.sundsvall.ai.flow.service.execution;

import java.util.UUID;
import se.sundsvall.ai.flow.model.flowdefinition.Step;

/**
 * Abstract base for target executors. Replaces the previous TargetExecutor interface so implementations can extend this
 * class. Keeps nested TargetResult contract.
 */
public abstract class TargetExecutor {
	public abstract boolean supports(final Step.Target.Type type);

	public abstract TargetResult execute(final StepRunContext stepRunContext) throws InterruptedException;

	public record TargetResult(String output, UUID runId, UUID sessionId) {

	}
}
