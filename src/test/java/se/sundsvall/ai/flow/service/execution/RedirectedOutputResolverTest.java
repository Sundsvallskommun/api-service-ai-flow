package se.sundsvall.ai.flow.service.execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

import java.util.List;
import org.junit.jupiter.api.Test;
import se.sundsvall.ai.flow.model.flowdefinition.Flow;
import se.sundsvall.ai.flow.model.flowdefinition.RedirectedOutput;
import se.sundsvall.ai.flow.model.flowdefinition.Step;
import se.sundsvall.ai.flow.model.session.Session;
import se.sundsvall.ai.flow.model.session.StepExecutionFactory;
import se.sundsvall.ai.flow.model.support.StringMultipartFile;

class RedirectedOutputResolverTest {

	@Test
	void addsRedirectedOutputAsInput() {
		// Arrange: S2 depends on S1 via redirected output
		final var s1 = new Step().withId("S1").withName("Step One").withOrder(1);
		final var s2 = new Step().withId("S2").withName("Step Two").withOrder(2)
			.withInputs(List.of(new RedirectedOutput().withStep("S1").withUseAs("Use As Name")));
		final var flow = new Flow().withSteps(List.of(s1, s2));
		final var session = new Session("2281", flow, new StepExecutionFactory());

		// give S1 an output
		session.getStepExecution("S1").setOutput("the-output");

		final var resolver = new RedirectedOutputResolver();

		// Act
		resolver.addRedirectedOutputsAsInputs(session, s2);

		// Assert
		final var redirectedInputs = session.getRedirectedOutputInput().get("S1");
		assertThat(redirectedInputs).hasSize(1);
		assertThat(redirectedInputs.get(0).getFile()).asInstanceOf(type(StringMultipartFile.class))
			.satisfies(file -> {
				assertThat(file.getName()).isEqualTo("Use As Name");
				assertThat(file.getValue()).isEqualTo("the-output");
			});
	}
}
