package se.sundsvall.ai.flow.service.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.sundsvall.ai.flow.model.session.StepExecution;

/** Ensures required steps are executed before the current step. */
@Component
public class RequiredStepsRunner {
	private static final Logger LOG = LoggerFactory.getLogger(RequiredStepsRunner.class);

	public void ensureRequiredStepsExecuted(
		final StepExecution execution,
		final String userInput,
		final boolean runRequiredSteps,
		final java.util.function.BiConsumer<StepExecution, String> stepInvoker) {
		if (!runRequiredSteps) {
			return;
		}
		for (final var required : execution.getRequiredStepExecutions()) {
			if (required.getState() != StepExecution.State.DONE) {
				LOG.info("Triggering required step '{}' for '{}'", required.getStep().getName(), execution.getStep().getName());
				stepInvoker.accept(required, userInput);
			}
		}
	}
}
