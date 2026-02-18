package se.sundsvall.ai.flow.service.execution;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.ai.flow.model.flowdefinition.Flow;
import se.sundsvall.ai.flow.model.flowdefinition.RedirectedOutput;
import se.sundsvall.ai.flow.model.flowdefinition.Step;
import se.sundsvall.ai.flow.model.session.Session;
import se.sundsvall.ai.flow.model.session.StepExecution;
import se.sundsvall.ai.flow.model.session.StepExecutionFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.ai.flow.model.session.Session.State.ERROR;

@ExtendWith(MockitoExtension.class)
class SessionOrchestratorTest {

	@Mock
	private FileUploadManager fileUploadManager;

	@Mock
	private StepRunner stepRunner;

	@InjectMocks
	private SessionOrchestrator orchestrator;

	@AfterEach
	void verifyAll() {
		verifyNoMoreInteractions(fileUploadManager, stepRunner);
	}

	private Session newSessionWithTwoSteps() {
		final var step1 = new Step().withId("S1").withName("S1").withOrder(1);
		final var step2 = new Step().withId("S2").withName("S2").withOrder(2);
		final var flow = new Flow().withSteps(List.of(step1, step2));
		return new Session("2281", flow, new StepExecutionFactory());
	}

	@Test
	void runSession_happyPath() {
		final var session = newSessionWithTwoSteps();

		when(stepRunner.runStep(any())).thenReturn(new StepExecutionResult(true, "ok", UUID.randomUUID(), null, null));

		orchestrator.runSession("2281", session);

		verify(fileUploadManager).uploadMissing("2281", session);
		assertThat(session.getState()).isEqualTo(Session.State.FINISHED);
	}

	@Test
	void runSession_stepFailureSetsSessionError() {
		final var step1 = new Step().withId("S1").withName("S1").withOrder(1);
		final var flow = new Flow().withSteps(List.of(step1));
		final var session = new Session("2281", flow, new StepExecutionFactory());

		when(stepRunner.runStep(any())).thenReturn(StepExecutionResult.failure("boom"));

		orchestrator.runSession("2281", session);

		verify(fileUploadManager).uploadMissing("2281", session);
		assertThat(session.getState()).isEqualTo(ERROR);
	}

	@Test
	void runSession_stepsWithDependenciesExecuteInCorrectOrder() {
		// Create step1 (no dependencies)
		final var step1 = new Step().withId("S1").withName("Step 1").withOrder(1);

		// Create step2 that depends on step1's output
		final var redirectedOutput = new RedirectedOutput().withStep("S1").withUseAs("input");
		final var step2 = new Step().withId("S2").withName("Step 2").withOrder(2).withInputs(List.of(redirectedOutput));

		// Create step3 that depends on step2's output
		final var redirectedOutput2 = new RedirectedOutput().withStep("S2").withUseAs("input");
		final var step3 = new Step().withId("S3").withName("Step 3").withOrder(3).withInputs(List.of(redirectedOutput2));

		final var flow = new Flow().withSteps(List.of(step1, step2, step3));
		final var session = new Session("2281", flow, new StepExecutionFactory());

		// Track execution order
		final AtomicInteger executionCounter = new AtomicInteger(0);
		final AtomicInteger step1Order = new AtomicInteger(-1);
		final AtomicInteger step2Order = new AtomicInteger(-1);
		final AtomicInteger step3Order = new AtomicInteger(-1);

		when(stepRunner.runStep(any())).thenAnswer(invocation -> {
			final StepRunContext context = invocation.getArgument(0);
			final String stepId = context.stepExecution().getStep().getId();
			final int order = executionCounter.getAndIncrement();

			switch (stepId) {
				case "S1" -> step1Order.set(order);
				case "S2" -> step2Order.set(order);
				case "S3" -> step3Order.set(order);
				default -> throw new IllegalStateException("Unexpected value: " + stepId);
			}

			return new StepExecutionResult(true, "output-" + stepId, UUID.randomUUID(), null, null);
		});

		orchestrator.runSession("2281", session);

		// Verify execution completed successfully
		assertThat(session.getState()).isEqualTo(Session.State.FINISHED);

		// Verify all steps executed
		assertThat(step1Order.get()).isNotEqualTo(-1);
		assertThat(step2Order.get()).isNotEqualTo(-1);
		assertThat(step3Order.get()).isNotEqualTo(-1);

		// Verify correct execution order: S1 before S2, S2 before S3
		assertThat(step1Order.get()).isLessThan(step2Order.get());
		assertThat(step2Order.get()).isLessThan(step3Order.get());

		// Verify all steps are marked as DONE
		assertThat(session.getStepExecution("S1").getState()).isEqualTo(StepExecution.State.DONE);
		assertThat(session.getStepExecution("S2").getState()).isEqualTo(StepExecution.State.DONE);
		assertThat(session.getStepExecution("S3").getState()).isEqualTo(StepExecution.State.DONE);

		verify(fileUploadManager).uploadMissing("2281", session);
	}

	@Test
	void runSession_independentStepsCanExecuteInParallel() {
		// Create three independent steps (no dependencies)
		final var step1 = new Step().withId("S1").withName("Step 1").withOrder(1);
		final var step2 = new Step().withId("S2").withName("Step 2").withOrder(2);
		final var step3 = new Step().withId("S3").withName("Step 3").withOrder(3);

		final var flow = new Flow().withSteps(List.of(step1, step2, step3));
		final var session = new Session("2281", flow, new StepExecutionFactory());

		when(stepRunner.runStep(any())).thenReturn(new StepExecutionResult(true, "ok", UUID.randomUUID(), null, null));

		orchestrator.runSession("2281", session);

		// Verify all steps completed successfully
		assertThat(session.getState()).isEqualTo(Session.State.FINISHED);
		assertThat(session.getStepExecution("S1").getState()).isEqualTo(StepExecution.State.DONE);
		assertThat(session.getStepExecution("S2").getState()).isEqualTo(StepExecution.State.DONE);
		assertThat(session.getStepExecution("S3").getState()).isEqualTo(StepExecution.State.DONE);

		verify(fileUploadManager).uploadMissing("2281", session);
		verify(stepRunner, times(3)).runStep(any());
	}

	@Test
	void runSession_stepWithErrorMessage() {
		final var step1 = new Step().withId("S1").withName("S1").withOrder(1);
		final var flow = new Flow().withSteps(List.of(step1));
		final var session = new Session("2281", flow, new StepExecutionFactory());

		when(stepRunner.runStep(any())).thenReturn(StepExecutionResult.failure("Something went wrong"));

		orchestrator.runSession("2281", session);

		assertThat(session.getState()).isEqualTo(ERROR);
		assertThat(session.getStepExecution("S1").getState()).isEqualTo(StepExecution.State.ERROR);
		assertThat(session.getStepExecution("S1").getErrorMessage()).isEqualTo("Something went wrong");
		verify(fileUploadManager).uploadMissing("2281", session);
	}

	@Test
	void runSession_successWithEneoRunIdAndSessionId() {
		final var step1 = new Step().withId("S1").withName("S1").withOrder(1);
		final var flow = new Flow().withSteps(List.of(step1));
		final var session = new Session("2281", flow, new StepExecutionFactory());

		final var runId = UUID.randomUUID();
		final var sessionId = UUID.randomUUID();
		when(stepRunner.runStep(any())).thenReturn(new StepExecutionResult(true, "output", runId, sessionId, null));

		orchestrator.runSession("2281", session);

		assertThat(session.getState()).isEqualTo(Session.State.FINISHED);
		assertThat(session.getStepExecution("S1").getEneoRunId()).isEqualTo(runId);
		assertThat(session.getStepExecution("S1").getEneoSessionId()).isEqualTo(sessionId);
		assertThat(session.getStepExecution("S1").getOutput()).isEqualTo("output");

		verify(fileUploadManager).uploadMissing("2281", session);
		verify(stepRunner).runStep(any());
	}

	@Test
	void runSession_successWithNullRunIdAndSessionId() {
		final var step1 = new Step().withId("S1").withName("S1").withOrder(1);
		final var flow = new Flow().withSteps(List.of(step1));
		final var session = new Session("2281", flow, new StepExecutionFactory());

		when(stepRunner.runStep(any())).thenReturn(new StepExecutionResult(true, "output", null, null, null));

		orchestrator.runSession("2281", session);

		assertThat(session.getState()).isEqualTo(Session.State.FINISHED);
		assertThat(session.getStepExecution("S1").getEneoRunId()).isNull();
		assertThat(session.getStepExecution("S1").getEneoSessionId()).isNull();

		verify(fileUploadManager).uploadMissing("2281", session);
	}

	@Test
	void runSession_stepInRunningState() {
		final var step1 = new Step().withId("S1").withName("S1").withOrder(1);
		final var flow = new Flow().withSteps(List.of(step1));
		final var session = new Session("2281", flow, new StepExecutionFactory());

		when(stepRunner.runStep(any())).thenReturn(new StepExecutionResult(true, "ok", UUID.randomUUID(), null, null));

		orchestrator.runSession("2281", session);

		// During execution, steps are set to RUNNING, then to DONE
		assertThat(session.getState()).isEqualTo(Session.State.FINISHED);
		assertThat(session.getStepExecution("S1").getState()).isEqualTo(StepExecution.State.DONE);

		verify(fileUploadManager).uploadMissing("2281", session);
	}

	@Test
	void runSession_stepAlreadyDone() {
		final var step1 = new Step().withId("S1").withName("S1").withOrder(1);
		final var flow = new Flow().withSteps(List.of(step1));
		final var session = new Session("2281", flow, new StepExecutionFactory());

		// Manually set step to DONE
		session.getStepExecution("S1").setState(StepExecution.State.DONE);

		orchestrator.runSession("2281", session);

		// Session should finish immediately since step is already done
		assertThat(session.getState()).isEqualTo(Session.State.FINISHED);
		verify(fileUploadManager).uploadMissing("2281", session);
	}

	@Test
	void runSession_stepAlreadyError() {
		final var step1 = new Step().withId("S1").withName("S1").withOrder(1);
		final var flow = new Flow().withSteps(List.of(step1));
		final var session = new Session("2281", flow, new StepExecutionFactory());

		// Manually set the step to ERROR
		session.getStepExecution("S1").setState(StepExecution.State.ERROR);

		orchestrator.runSession("2281", session);

		// Session should finish immediately since step is in error state
		assertThat(session.getState()).isEqualTo(Session.State.FINISHED);
		verify(fileUploadManager).uploadMissing("2281", session);
	}

	@Test
	void runSession_stepRunnerThrowsException() {
		final var step1 = new Step().withId("S1").withName("S1").withOrder(1);
		final var flow = new Flow().withSteps(List.of(step1));
		final var session = new Session("2281", flow, new StepExecutionFactory());

		// Make stepRunner throw a RuntimeException
		when(stepRunner.runStep(any())).thenThrow(new RuntimeException("Unexpected error"));

		orchestrator.runSession("2281", session);

		// Session should be in ERROR state due to ExecutionException
		assertThat(session.getState()).isEqualTo(ERROR);
		verify(fileUploadManager).uploadMissing("2281", session);
	}

	@Test
	void runSession_deadlockDetection_stepStuckInRunning() {
		final var step1 = new Step().withId("S1").withName("S1").withOrder(1);
		final var flow = new Flow().withSteps(List.of(step1));
		final var session = new Session("2281", flow, new StepExecutionFactory());

		// Manually set step to RUNNING to simulate a stuck step
		session.getStepExecution("S1").setState(StepExecution.State.RUNNING);

		orchestrator.runSession("2281", session);

		// Session should be in ERROR state due to deadlock detection
		assertThat(session.getState()).isEqualTo(ERROR);
		verify(fileUploadManager).uploadMissing("2281", session);
	}

	@Test
	void runSession_multipleStepsWithOneStuckInRunning() {
		final var step1 = new Step().withId("S1").withName("S1").withOrder(1);
		final var step2 = new Step().withId("S2").withName("S2").withOrder(2);
		final var flow = new Flow().withSteps(List.of(step1, step2));
		final var session = new Session("2281", flow, new StepExecutionFactory());

		// Set S1 to DONE but S2 to RUNNING (stuck)
		session.getStepExecution("S1").setState(StepExecution.State.DONE);
		session.getStepExecution("S2").setState(StepExecution.State.RUNNING);

		orchestrator.runSession("2281", session);

		// Session should detect deadlock and set to ERROR
		assertThat(session.getState()).isEqualTo(ERROR);
		verify(fileUploadManager).uploadMissing("2281", session);
	}
}
