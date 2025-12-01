package se.sundsvall.ai.flow.service.execution;

import java.util.function.BiConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.sundsvall.ai.flow.model.session.StepExecution;

/** Ensures required steps are executed before the current step. */
@Component
public class RequiredStepsRunner {
	private static final Logger LOG = LoggerFactory.getLogger(RequiredStepsRunner.class);

	public void ensureRequiredStepsExecuted(final StepExecution execution, final String userInput, final boolean runRequiredSteps, final BiConsumer<StepExecution, String> stepInvoker) {
		if (!runRequiredSteps) {
			return;
		}
		for (final var requiredStepExecution : execution.getRequiredStepExecutions()) {
			if (requiredStepExecution.getState() != StepExecution.State.DONE) {
				LOG.info("Triggering required step '{}' for '{}'", requiredStepExecution.getStep().getName(), execution.getStep().getName());
				stepInvoker.accept(requiredStepExecution, userInput);
			}
		}
	}
}
