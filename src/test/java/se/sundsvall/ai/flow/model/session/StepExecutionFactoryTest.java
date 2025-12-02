package se.sundsvall.ai.flow.model.session;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import se.sundsvall.ai.flow.model.flowdefinition.Flow;
import se.sundsvall.ai.flow.model.flowdefinition.RedirectedOutput;
import se.sundsvall.ai.flow.model.flowdefinition.Step;

class StepExecutionFactoryTest {

	@Test
	void wiresRedirectedOutputsAsRequiredExecutions() {
		// Arrange: step2 depends on step1 via redirected output
		final var step1 = new Step().withId("step1").withOrder(1);
		final var step2 = new Step().withId("step2").withOrder(2)
			.withInputs(List.of(new RedirectedOutput().withStep("step1").withUseAs("use-as")));

		final var flow = new Flow().withSteps(List.of(step1, step2));
		final var session = new Session("2281", flow, new StepExecutionFactory());

		// Act
		final var stepExecution1 = session.getStepExecution("step1");
		final var stepExecution2 = session.getStepExecution("step2");

		// Assert
		assertThat(stepExecution1).isNotNull();
		assertThat(stepExecution2).isNotNull();
		assertThat(stepExecution2.getRequiredStepExecutions()).containsExactly(stepExecution1);
	}
}
