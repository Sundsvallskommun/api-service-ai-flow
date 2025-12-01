package se.sundsvall.ai.flow.service.execution;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import se.sundsvall.ai.flow.model.flowdefinition.Flow;
import se.sundsvall.ai.flow.model.flowdefinition.RedirectedOutput;
import se.sundsvall.ai.flow.model.flowdefinition.Step;
import se.sundsvall.ai.flow.model.session.Session;
import se.sundsvall.ai.flow.model.session.StepExecution;
import se.sundsvall.ai.flow.model.session.StepExecutionFactory;

class RequiredStepsRunnerTest {

	@Test
	void triggersRequiredStepsWhenEnabledAndNotDone() {
		final var step1 = new Step().withId("S1").withName("S1").withOrder(1);
		final var step2 = new Step().withId("S2").withName("S2").withOrder(2)
			.withInputs(List.of(new RedirectedOutput().withStep("S1").withUseAs("use")));
		final var flow = new Flow().withSteps(List.of(step1, step2));
		final var session = new Session("2281", flow, new StepExecutionFactory());

		final var stepExecution = session.getStepExecution("S2");
		final var runner = new RequiredStepsRunner();

		final AtomicInteger invoked = new AtomicInteger();
		runner.ensureRequiredStepsExecuted(stepExecution, null, true, (required, in) -> {
			invoked.incrementAndGet();
			// mark the required step as done to prevent re-invocation
			required.setState(StepExecution.State.DONE);
		});

		assertThat(invoked.get()).isEqualTo(1);
	}

	@Test
	void doesNotTriggerWhenDisabled() {
		final var step1 = new Step().withId("S1").withName("S1").withOrder(1);
		final var step2 = new Step().withId("S2").withName("S2").withOrder(2)
			.withInputs(List.of(new RedirectedOutput().withStep("S1").withUseAs("use")));
		final var flow = new Flow().withSteps(List.of(step1, step2));
		final var session = new Session("2281", flow, new StepExecutionFactory());
		final var stepExecution = session.getStepExecution("S2");
		final var runner = new RequiredStepsRunner();

		final AtomicInteger invoked = new AtomicInteger();
		runner.ensureRequiredStepsExecuted(stepExecution, null, false, (required, in) -> invoked.incrementAndGet());

		assertThat(invoked.get()).isZero();
	}
}
