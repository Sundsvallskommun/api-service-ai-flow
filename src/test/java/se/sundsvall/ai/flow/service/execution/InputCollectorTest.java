package se.sundsvall.ai.flow.service.execution;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import se.sundsvall.ai.flow.model.flowdefinition.Flow;
import se.sundsvall.ai.flow.model.flowdefinition.FlowInput;
import se.sundsvall.ai.flow.model.flowdefinition.FlowInputRef;
import se.sundsvall.ai.flow.model.flowdefinition.RedirectedOutput;
import se.sundsvall.ai.flow.model.flowdefinition.Step;
import se.sundsvall.ai.flow.model.session.Session;
import se.sundsvall.ai.flow.model.session.StepExecutionFactory;
import se.sundsvall.ai.flow.model.session.TextInputValue;
import se.sundsvall.ai.flow.service.InputDescriptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InputCollectorTest {

	@Test
	void resolveCollectsInputsAndBuildsInfo() {
		// Arrange
		final var flowInputA = new FlowInput().withId("A").withName("Doc A").withMultipleValued(true);
		final var flowInputB = new FlowInput().withId("B").withName("Doc B").withMultipleValued(true);

		final var step1 = new Step().withId("S1").withName("Step One").withOrder(1);
		final var step2 = new Step().withId("S2").withName("Step Two").withOrder(2)
			.withInputs(List.of(new FlowInputRef().withInput("A"), new RedirectedOutput().withStep("S1").withUseAs("S1 out")));

		final var flow = new Flow().withFlowInputs(List.of(flowInputA, flowInputB)).withSteps(List.of(step1, step2));
		final var session = new Session("2281", flow, new StepExecutionFactory());

		// Add A twice; set eneo ids
		session.addSimpleInput("A", "text1");
		session.addSimpleInput("A", "text2");
		final var idsA = List.of(UUID.randomUUID(), UUID.randomUUID());
		session.getInput().get("A").get(0).setEneoFileId(idsA.get(0));
		session.getInput().get("A").get(1).setEneoFileId(idsA.get(1));

		// Add redirected output from S1 and set id
		session.addRedirectedOutputAsInput("S1", new TextInputValue("S1 out", "hello"));
		final var redirectedId = UUID.randomUUID();
		session.getRedirectedOutputInput().get("S1").getFirst().setEneoFileId(redirectedId);

		// Mock descriptor to return canned strings
		final var descriptor = mock(InputDescriptor.class);
		when(descriptor.describe(session)).thenReturn(Map.of(
			"A", "infoA",
			"S1", "infoS1"));

		final var collector = new InputCollector(descriptor);

		// Act
		final var inputs = collector.resolve(session, step2);

		// Assert
		assertThat(inputs.inputsInUse()).containsExactlyInAnyOrder("A", "S1");
		assertThat(inputs.inputsInUseInfo()).isEqualTo("infoA" + "infoS1");
		assertThat(inputs.fileIdsInUse()).contains(idsA.get(0), idsA.get(1), redirectedId);
	}
}
