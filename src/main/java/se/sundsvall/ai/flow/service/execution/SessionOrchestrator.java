package se.sundsvall.ai.flow.service.execution;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.sundsvall.ai.flow.model.session.Session;
import se.sundsvall.ai.flow.model.session.StepExecution;

/**
 * Orchestrates the execution of flow sessions by running steps in parallel where possible. Steps are executed in
 * "waves" where each wave contains all steps whose dependencies have completed.
 */
@Component
public class SessionOrchestrator {

	private static final Logger LOG = LoggerFactory.getLogger(SessionOrchestrator.class);

	private final FileUploadManager fileUploadManager;
	private final StepRunner stepRunner;

	public SessionOrchestrator(final FileUploadManager fileUploadManager, final StepRunner stepRunner) {
		this.fileUploadManager = fileUploadManager;
		this.stepRunner = stepRunner;
	}

	/**
	 * Runs the given session by executing steps in parallel where possible. Steps are executed in "waves" where each wave
	 * contains all steps whose dependencies have completed.
	 *
	 * @param municipalityId the municipality ID
	 * @param session        the session to run
	 */
	public void runSession(final String municipalityId, final Session session) {
		fileUploadManager.uploadMissing(municipalityId, session);
		session.setState(Session.State.RUNNING);

		final var allSteps = new ArrayList<>(session.getStepExecutions().values());

		while (true) {
			final var unfinished = getUnfinishedSteps(allSteps);

			if (unfinished.isEmpty()) {
				session.setState(Session.State.FINISHED);
				return;
			}

			final var runnable = getRunnableSteps(unfinished);

			if (runnable.isEmpty()) {
				LOG.error("No runnable steps found while session {} still has unfinished steps - possible dependency deadlock", session.getId());
				session.setState(Session.State.ERROR);
				return;
			}

			if (!executeWave(municipalityId, session, runnable)) {
				return;
			}
		}
	}

	/**
	 * Gets the list of unfinished steps from the provided list of all steps.
	 *
	 * @param  allSteps the list of all steps
	 * @return          the list of unfinished steps
	 */
	private List<StepExecution> getUnfinishedSteps(final List<StepExecution> allSteps) {
		return allSteps.stream()
			.filter(se -> se.getState() != StepExecution.State.DONE && se.getState() != StepExecution.State.ERROR)
			.toList();
	}

	/**
	 * Gets the list of runnable steps from the provided list of unfinished steps. A step is considered runnable if it is
	 * not already running and all its required steps are done.
	 *
	 * @param  unfinished the list of unfinished steps
	 * @return            the list of runnable steps
	 */
	private List<StepExecution> getRunnableSteps(final List<StepExecution> unfinished) {
		return unfinished.stream()
			.filter(se -> se.getState() != StepExecution.State.RUNNING)
			.filter(this::areAllRequiredStepsDone)
			.toList();
	}

	/**
	 * Checks if all required steps for the given step execution are done.
	 *
	 * @param  stepExecution the step execution to check
	 * @return               true if all required steps are done, false otherwise
	 */
	private boolean areAllRequiredStepsDone(final StepExecution stepExecution) {
		return stepExecution.getRequiredStepExecutions().stream()
			.allMatch(req -> req.getState() == StepExecution.State.DONE);
	}

	/**
	 * Executes a wave of steps in parallel. A wave is a list of steps that can be executed concurrently because all their
	 * dependencies have been satisfied. The method submits all steps for execution and waits for their completion, then
	 * processes the
	 * results to determine if the wave succeeded or failed.
	 *
	 * @param  municipalityId the municipality ID
	 * @param  session        the session being executed
	 * @param  runnable       the list of steps to execute in this wave
	 * @return                true if all steps in the wave succeeded, false if any step failed
	 */
	private boolean executeWave(final String municipalityId, final Session session, final List<StepExecution> runnable) {
		final var futures = submitStepsForExecution(municipalityId, session, runnable);

		try {
			CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
			LOG.error("Session execution interrupted for session {}", session.getId());
			session.setState(Session.State.ERROR);
			return false;
		} catch (final ExecutionException e) {
			LOG.error("Execution failed while running steps for session {}: {}", session.getId(), e.getMessage(), e);
			session.setState(Session.State.ERROR);
			return false;
		}

		return processWaveResults(session, runnable, futures);
	}

	/**
	 * Submits the given list of steps for execution in parallel.
	 *
	 * @param  municipalityId the municipality ID
	 * @param  session        the session being executed
	 * @param  runnable       the list of steps to execute
	 * @return                a list of CompletableFutures representing the results of the step executions
	 */
	private List<CompletableFuture<StepExecutionResult>> submitStepsForExecution(final String municipalityId, final Session session, final List<StepExecution> runnable) {

		final var futures = new ArrayList<CompletableFuture<StepExecutionResult>>();
		for (final var stepExecution : runnable) {
			stepExecution.setState(StepExecution.State.RUNNING);
			final var context = new StepRunContext(municipalityId, session, stepExecution, List.of(), List.of(), "", null, true);
			futures.add(CompletableFuture.supplyAsync(() -> stepRunner.runStep(context)));
		}
		return futures;
	}

	/**
	 * Processes the results from a wave execution. Checks each result for success or failure, updates the corresponding
	 * StepExecution objects, and handles any failures by logging errors and updating the session state.
	 *
	 * @param  session  the session being executed
	 * @param  runnable the list of steps that were executed
	 * @param  futures  the list of CompletableFutures representing the step execution results
	 * @return          true if all steps in the wave succeeded, false if any step failed
	 */
	private boolean processWaveResults(final Session session, final List<StepExecution> runnable, final List<CompletableFuture<StepExecutionResult>> futures) {

		for (int i = 0; i < futures.size(); i++) {
			final var stepExecution = runnable.get(i);
			final var result = getResultSafely(session, futures.get(i));

			if (result == null) {
				return false;
			}

			if (!result.success()) {
				handleStepFailure(session, stepExecution, result);
				return false;
			}

			applySuccessResult(stepExecution, result);
		}
		return true;
	}

	/**
	 * Safely retrieves the result of a step execution from the given CompletableFuture.
	 *
	 * @param  session the session being executed
	 * @param  future  the CompletableFuture representing the result of the step execution
	 * @return         the StepExecutionResult if available, null if an error occurred
	 */
	private StepExecutionResult getResultSafely(final Session session, final CompletableFuture<StepExecutionResult> future) {
		try {
			return future.getNow(null);
		} catch (final Exception e) {
			LOG.warn("Unable to retrieve step result for session {}: {}", session.getId(), e.getMessage());
			session.setState(Session.State.ERROR);
			return null;
		}
	}

	/**
	 * Handles a step failure by logging the error, updating the step execution state, and setting the session state to
	 * ERROR.
	 *
	 * @param session       the session being executed
	 * @param stepExecution the step execution that failed
	 * @param result        the result of the step execution
	 */
	private void handleStepFailure(final Session session, final StepExecution stepExecution, final StepExecutionResult result) {
		LOG.error("Step in session {} failed with message: {}", session.getId(), result.errorMessage());
		stepExecution.setErrorMessage(result.errorMessage());
		stepExecution.setState(StepExecution.State.ERROR);
		session.setState(Session.State.ERROR);
	}

	/**
	 * Applies the successful result of a step execution to the corresponding StepExecution object.
	 *
	 * @param stepExecution the step execution to update
	 * @param result        the result of the step execution
	 */
	private void applySuccessResult(final StepExecution stepExecution, final StepExecutionResult result) {
		if (result.runId() != null) {
			stepExecution.setEneoRunId(result.runId());
		}
		if (result.sessionId() != null) {
			stepExecution.setEneoSessionId(result.sessionId());
		}
		stepExecution.setOutput(result.output());
		stepExecution.setState(StepExecution.State.DONE);
	}
}
