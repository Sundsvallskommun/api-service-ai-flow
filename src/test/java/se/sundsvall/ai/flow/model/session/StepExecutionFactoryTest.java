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
		// Arrange: S2 depends on S1 via redirected output
		final var s1 = new Step().withId("S1").withOrder(1);
		final var s2 = new Step().withId("S2").withOrder(2)
			.withInputs(List.of(new RedirectedOutput().withStep("S1").withUseAs("use-as")));

		final var flow = new Flow().withSteps(List.of(s1, s2));
		final var session = new Session("2281", flow, new StepExecutionFactory());

		// Act
		final var exec1 = session.getStepExecution("S1");
		final var exec2 = session.getStepExecution("S2");

		// Assert
		assertThat(exec1).isNotNull();
		assertThat(exec2).isNotNull();
		assertThat(exec2.getRequiredStepExecutions()).containsExactly(exec1);
	}
}
