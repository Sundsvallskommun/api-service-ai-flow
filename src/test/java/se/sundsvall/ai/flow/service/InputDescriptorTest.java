package se.sundsvall.ai.flow.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import se.sundsvall.ai.flow.model.flowdefinition.Flow;
import se.sundsvall.ai.flow.model.flowdefinition.FlowInput;
import se.sundsvall.ai.flow.model.flowdefinition.RedirectedOutput;
import se.sundsvall.ai.flow.model.flowdefinition.Step;
import se.sundsvall.ai.flow.model.session.Session;
import se.sundsvall.ai.flow.model.session.StepExecutionFactory;

class InputDescriptorTest {

	@Test
	void describesRequiredOptionalAndRedirectedInputs() {
		// Flow inputs: required A, optional B
		final var inputA = new FlowInput().withId("A").withName("Required Doc").withMultipleValued(true).withOptional(false);
		final var inputB = new FlowInput().withId("B").withName("Optional Doc").withMultipleValued(true).withOptional(true);

		// Steps: S1 (no inputs), S2 consumes redirected output from S1
		final var s1 = new Step().withId("S1").withName("Step One").withOrder(1);
		final var s2 = new Step().withId("S2").withName("Step Two").withOrder(2)
			.withInputs(List.of(new RedirectedOutput().withStep("S1").withUseAs("S1 output")));

		final var flow = new Flow().withFlowInputs(List.of(inputA, inputB)).withSteps(List.of(s1, s2));

		final var session = new Session("2281", flow, new StepExecutionFactory());

		// Add two files for A; set uploaded file ids
		session.addSimpleInput("A", "ignored-text-1");
		session.addSimpleInput("A", "ignored-text-2");
		final var idsA = List.of(UUID.randomUUID(), UUID.randomUUID());
		// Mutate underlying Input to set Intric ids
		final var listA = session.getInput().get("A");
		listA.get(0).setEneoFileId(idsA.get(0));
		listA.get(1).setEneoFileId(idsA.get(1));

		// Optional B remains empty -> should be omitted

		// Add redirected output for S1 as input (as text), then set an id
		session.addRedirectedOutputAsInput("S1", new se.sundsvall.ai.flow.model.session.TextInputValue("S1 output", "text"));
		final var redirectedId = UUID.randomUUID();
		session.getRedirectedOutputInput().get("S1").getFirst().setEneoFileId(redirectedId);

		final var descriptor = new InputDescriptor();
		final Map<String, String> info = descriptor.describe(session);

		assertThat(info).containsOnlyKeys("A", "S1");
		assertThat(info.get("A")).contains("du hittar", "filen/filerna");
		assertThat(info.get("A")).contains(idsA.get(0).toString()).contains(idsA.get(1).toString());
		assertThat(info.get("S1")).contains(redirectedId.toString());
	}
}
