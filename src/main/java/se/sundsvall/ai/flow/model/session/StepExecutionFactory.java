package se.sundsvall.ai.flow.model.session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import se.sundsvall.ai.flow.model.flowdefinition.Flow;
import se.sundsvall.ai.flow.model.flowdefinition.RedirectedOutput;
import se.sundsvall.ai.flow.model.flowdefinition.Step;

import static java.util.Objects.requireNonNull;

/**
 * Default implementation that mirrors the previous wiring behavior in Session.
 */
public class StepExecutionFactory {

	public Map<String, StepExecution> createStepExecutions(final Session session, final Flow flow) {
		requireNonNull(session);
		requireNonNull(flow);

		final Map<String, StepExecution> stepExecutionMap = new HashMap<>();
		// Create initial (empty) executions for all steps, wiring dependencies recursively
		flow.getSteps().forEach(step -> createForStep(session, flow, step, stepExecutionMap));
		return stepExecutionMap;
	}

	private StepExecution createForStep(final Session session, final Flow flow, final Step step, final Map<String, StepExecution> stepExecutionMap) {
		// Return existing if already created
		final var existing = stepExecutionMap.get(step.getId());
		if (existing != null) {
			return existing;
		}

		// Collect required step executions from redirected outputs
		final List<StepExecution> required = new ArrayList<>();
		for (final var stepInput : step.getInputs()) {
			if (stepInput instanceof final RedirectedOutput redirectedOutput) {
				final var sourceStepId = redirectedOutput.getStep();
				final var sourceStep = flow.getStep(sourceStepId);
				required.add(createForStep(session, flow, sourceStep, stepExecutionMap));
			}
		}

		final var stepExecution = new StepExecution(session, step, required);
		stepExecutionMap.put(step.getId(), stepExecution);
		return stepExecution;
	}
}
