package se.sundsvall.ai.flow.service.execution;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.ai.flow.model.flowdefinition.Flow;
import se.sundsvall.ai.flow.model.flowdefinition.Step;
import se.sundsvall.ai.flow.model.session.Session;
import se.sundsvall.ai.flow.model.session.StepExecutionFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StepRunnerTest {

	@Mock
	private RequiredStepsRunner requiredStepsRunner;

	@Mock
	private StepStateUpdater stepStateUpdater;

	@Mock
	private InputPreparation inputPreparation;

	@Mock
	private TargetExecutorResolver targetExecutorResolver;

	@Mock
	private TargetExecutor targetExecutor;

	@Mock
	private TargetExecutor.TargetResult targetResult;

	@InjectMocks
	private StepRunner runner;

	private Session newSessionWithSingleStep() {
		final var step = new Step().withId("S1").withName("S1").withOrder(1).withTarget(new Step.Target(Step.Target.Type.SERVICE, UUID.randomUUID()));
		final var flow = new Flow().withSteps(List.of(step));
		return new Session("2281", flow, new StepExecutionFactory());
	}

	@Test
	void runStep_noExecutor() {
		final var session = newSessionWithSingleStep();
		session.setState(Session.State.RUNNING);
		final var stepExecution = session.getStepExecution("S1");

		when(inputPreparation.prepare("2281", session, stepExecution.getStep())).thenReturn(new InputCollector.Inputs(List.of(), "", List.of()));
		when(targetExecutorResolver.resolve(stepExecution.getStep().getTarget().type())).thenThrow(new RuntimeException("No TargetExecutor"));

		final var stepRunContext = new StepRunContext("2281", session, stepExecution, List.of(), List.of(), "", null, true);
		final var result = runner.runStep(stepRunContext);

		assertThat(result.success()).isFalse();
		assertThat(result.errorMessage()).contains("No TargetExecutor");

		verify(stepStateUpdater).markRunning(stepExecution);
		verify(stepStateUpdater).markFailed(eq(stepExecution), contains("No TargetExecutor"));
	}

	@Test
	void runStep_targetThrows() throws Exception {
		final var session = newSessionWithSingleStep();
		session.setState(Session.State.RUNNING);
		final var stepExecution = session.getStepExecution("S1");

		when(inputPreparation.prepare("2281", session, stepExecution.getStep())).thenReturn(new InputCollector.Inputs(List.of(), "", List.of()));

		when(targetExecutorResolver.resolve(stepExecution.getStep().getTarget().type())).thenReturn(targetExecutor);
		when(targetExecutor.execute(any())).thenThrow(new RuntimeException("boom"));

		final var stepRunContext = new StepRunContext("2281", session, stepExecution, List.of(), List.of(), "", null, true);
		final var result = runner.runStep(stepRunContext);

		assertThat(result.success()).isFalse();
		assertThat(result.errorMessage()).contains("boom");

		verify(stepStateUpdater).markRunning(stepExecution);
		verify(stepStateUpdater).markFailed(eq(stepExecution), contains("boom"));
	}

	@Test
	void runStep_requiredStepsInvoked() throws Exception {
		// Create a session with two steps where second requires first
		final var step1 = new Step().withId("S1").withName("S1").withOrder(1).withTarget(new Step.Target(Step.Target.Type.SERVICE, UUID.randomUUID()));
		final var step2 = new Step().withId("S2").withName("S2").withOrder(2).withTarget(new Step.Target(Step.Target.Type.SERVICE, UUID.randomUUID()));
		final var flow = new Flow().withSteps(List.of(step1, step2));
		final var session = new Session("2281", flow, new StepExecutionFactory());
		session.setState(Session.State.RUNNING);
		final var stepExecution = session.getStepExecution("S2");

		// When resolving inputs, return empty inputs
		when(inputPreparation.prepare("2281", session, stepExecution.getStep())).thenReturn(new InputCollector.Inputs(List.of(), "", List.of()));
		when(targetExecutorResolver.resolve(any(Step.Target.Type.class))).thenReturn(targetExecutor);

		// Mock the executor result and required method calls
		when(targetExecutor.execute(any(StepRunContext.class))).thenReturn(targetResult);
		when(targetResult.output()).thenReturn("ok");
		when(targetResult.runId()).thenReturn(UUID.randomUUID());
		when(targetResult.sessionId()).thenReturn(UUID.randomUUID());

		final var stepRunContext = new StepRunContext("2281", session, stepExecution, List.of(), List.of(), "", null, true);
		final var result = runner.runStep(stepRunContext);

		assertThat(result.success()).isTrue();
		verify(requiredStepsRunner).ensureRequiredStepsExecuted(eq(stepExecution), isNull(), eq(true), any());
	}
}
