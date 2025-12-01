package se.sundsvall.ai.flow.service.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StepRunner {

	private static final Logger LOG = LoggerFactory.getLogger(StepRunner.class);

	private final RequiredStepsRunner requiredStepsRunner;
	private final StepStateUpdater stepStateUpdater;
	private final InputPreparation inputPreparation;
	private final TargetExecutorResolver targetExecutorResolver;

	public StepRunner(final RequiredStepsRunner requiredStepsRunner,
		final InputPreparation inputPreparation,
		final TargetExecutorResolver targetExecutorResolver,
		final StepStateUpdater stepStateUpdater) {
		this.requiredStepsRunner = requiredStepsRunner;
		this.inputPreparation = inputPreparation;
		this.targetExecutorResolver = targetExecutorResolver;
		this.stepStateUpdater = stepStateUpdater;
	}

	public StepExecutionResult runStep(final StepRunContext context) {
		final var municipalityId = context.municipalityId();
		final var session = context.session();
		final var stepExecution = context.stepExecution();
		final var flow = session.getFlow();
		final var step = stepExecution.getStep();

		LOG.info("Executing step '{}' in flow '{}' for session {}", step.getName(), flow.getName(), session.getId());

		// Ensure required steps are executed first
		requiredStepsRunner.ensureRequiredStepsExecuted(stepExecution, context.userInput(), context.runRequiredSteps(),
			(requiredExec, userIn) -> runStep(new StepRunContext(municipalityId, session, requiredExec, context.inputsInUse(), context.inputFileIdsInUse(), context.inputsInUseInfo(), userIn, context.runRequiredSteps())));

		stepStateUpdater.markRunning(stepExecution);

		// Prepare inputs (redirected outputs, uploads and resolution)
		final var inputs = inputPreparation.prepare(municipalityId, session, step);

		// Build run context (ensure we use updated inputs)
		final var runContext = new StepRunContext(municipalityId, session, stepExecution, inputs.inputsInUse(), inputs.fileIdsInUse(), inputs.inputsInUseInfo(), context.userInput(), context.runRequiredSteps());

		try {
			final var targetExecutor = targetExecutorResolver.resolve(step.getTarget().type());

			final var result = targetExecutor.execute(runContext);

			stepStateUpdater.markFinished(stepExecution, result.output(), result.runId(), result.sessionId());
			return StepExecutionResult.success(result.output(), result.runId(), result.sessionId());
		} catch (final Exception e) {
			// Preserve interrupted status and handle failure
			LOG.error("Step execution failed for step {} in session {}: {}", step.getId(), session.getId(), e.getMessage(), e);
			stepStateUpdater.markFailed(stepExecution, e.getMessage());
			Thread.currentThread().interrupt();
			return StepExecutionResult.failure(e.getMessage());
		}
	}
}
