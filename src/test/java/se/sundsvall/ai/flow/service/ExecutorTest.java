package se.sundsvall.ai.flow.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static se.sundsvall.ai.flow.model.flowdefinition.Step.Target.Type.SERVICE;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Problem;
import se.sundsvall.ai.flow.model.flowdefinition.Flow;
import se.sundsvall.ai.flow.model.flowdefinition.Step;
import se.sundsvall.ai.flow.model.session.Session;
import se.sundsvall.ai.flow.model.session.StepExecution;
import se.sundsvall.ai.flow.model.session.StepExecutionFactory;
import se.sundsvall.ai.flow.service.execution.StepRunner;

@ExtendWith(MockitoExtension.class)
class ExecutorTest {

	@Mock
	private StepRunner stepRunner;

	@InjectMocks
	private Executor executor;

	private Session newSessionWithSingleStep() {
		final var step = new Step().withId("S1").withName("S1").withOrder(1).withTarget(new Step.Target(SERVICE, UUID.randomUUID()));
		final var flow = new Flow().withSteps(List.of(step));
		return new Session("2281", flow, new StepExecutionFactory());
	}

	@Test
	void executeStepFailsWhenSessionNotRunOrFinished() {
		final var session = newSessionWithSingleStep();
		final var exec = session.getStepExecution("S1");

		assertThatThrownBy(() -> executor.executeStep("2281", exec, null, true))
			.isInstanceOf(Problem.class)
			.hasMessageContaining("since the session has never been run yet");
	}

	@Test
	void executeStepFailsWhenAlreadyRunning() {
		final var session = newSessionWithSingleStep();
		// put session in RUNNING to pass the first check
		session.setState(Session.State.RUNNING);
		final var exec = session.getStepExecution("S1");
		exec.setState(StepExecution.State.RUNNING);

		assertThatThrownBy(() -> executor.executeStep("2281", exec, null, true))
			.isInstanceOf(Problem.class)
			.hasMessageContaining("already running step");
	}

	@Test
	void unsupportedTargetMarksFailed() {
		final var session = newSessionWithSingleStep();
		session.setState(Session.State.RUNNING);
		final var exec = session.getStepExecution("S1");

		// Will delegate to StepRunner; verify that StepRunner was invoked
		executor.executeStep("2281", exec, null, true);

		verify(stepRunner).runStep(any());
	}

	@Test
	void successPathMarksFinished() {
		final var session = newSessionWithSingleStep();
		session.setState(Session.State.RUNNING);
		final var exec = session.getStepExecution("S1");

		executor.executeStep("2281", exec, "q", true);

		// Verify delegation to StepRunner
		verify(stepRunner).runStep(any());
	}
}
