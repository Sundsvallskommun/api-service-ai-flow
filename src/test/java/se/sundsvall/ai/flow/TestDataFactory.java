package se.sundsvall.ai.flow;

import java.util.List;
import java.util.UUID;
import se.sundsvall.ai.flow.integration.db.model.FlowEntity;
import se.sundsvall.ai.flow.model.flowdefinition.Flow;
import se.sundsvall.ai.flow.model.flowdefinition.FlowInput;
import se.sundsvall.ai.flow.model.flowdefinition.FlowInputRef;
import se.sundsvall.ai.flow.model.flowdefinition.RedirectedOutput;
import se.sundsvall.ai.flow.model.flowdefinition.Step;
import se.sundsvall.ai.flow.model.flowdefinition.StepInput;
import se.sundsvall.ai.flow.model.session.Session;

import static se.sundsvall.ai.flow.model.flowdefinition.InputType.TEXT;

public final class TestDataFactory {

	public static final String MUNICIPALITY_ID = "2281";

	public static Session createSession() {
		final var flow = createFlow();
		final var session = new Session(MUNICIPALITY_ID, flow);
		flow.getFlowInputs().forEach(flowInput -> session.addSimpleInput(flowInput.getId(), "value"));
		return session;
	}

	public static Flow createFlow() {
		return new Flow()
			.withId("tjansteskrivelse")
			.withName("Tjänsteskrivelse")
			.withDescription("Ett Intric AI-flöde för tjänsteskrivelser")
			.withDefaultTemplateId("ai-mvp.tjansteskrivelse")
			.withFlowInputs(List.of(
				createFlowInput1(),
				createFlowInput2(),
				createFlowInput3(),
				createFlowInput4()))
			.withSteps(List.of(
				createStep1UsingService(),
				createStep2UsingService(),
				createStep3UsingService()));
	}

	public static FlowInput createFlowInput1() {
		return new FlowInput()
			.withId("input1")
			.withName("Input 1 (passthrough)")
			.withType(TEXT)
			.withPassthrough(true);
	}

	public static FlowInput createFlowInput2() {
		return new FlowInput()
			.withId("input2")
			.withName("Input 2")
			.withType(TEXT);
	}

	public static FlowInput createFlowInput3() {
		return new FlowInput()
			.withId("input3")
			.withName("Input 3")
			.withType(TEXT);
	}

	public static FlowInput createFlowInput4() {
		return new FlowInput()
			.withId("input4")
			.withName("Input 4")
			.withType(TEXT)
			.withMultipleValued(true);
	}

	public static Step createStep1UsingService() {
		return createStep1(Step.Target.Type.SERVICE);
	}

	public static Step createStep1UsingAssistant() {
		return createStep1(Step.Target.Type.ASSISTANT);
	}

	private static Step createStep1(final Step.Target.Type targetType) {
		return new Step()
			.withId("step1")
			.withName("Step 1")
			.withTarget(new Step.Target(targetType, UUID.randomUUID()))
			.withOrder(1)
			.withInputs(List.of(
				createFlowInputRef("input1"),
				createFlowInputRef("input2"),
				createFlowInputRef("input3")));
	}

	public static Step createStep2UsingService() {
		return createStep2(Step.Target.Type.SERVICE);
	}

	public static Step createStep2UsingAssistant() {
		return createStep2(Step.Target.Type.ASSISTANT);
	}

	private static Step createStep2(final Step.Target.Type targetType) {
		return new Step()
			.withId("step2")
			.withName("Step 2")
			.withTarget(new Step.Target(targetType, UUID.randomUUID()))
			.withOrder(2)
			.withInputs(List.of(
				createFlowInputRef("input2"),
				createFlowInputRef("input3"),
				createFlowInputRef("input4")));
	}

	public static Step createStep3UsingService() {
		return createStep3(Step.Target.Type.SERVICE);
	}

	public static Step createStep3UsingAssistant() {
		return createStep3(Step.Target.Type.ASSISTANT);
	}

	private static Step createStep3(final Step.Target.Type targetType) {
		return new Step()
			.withId("step3")
			.withName("Step 3")
			.withTarget(new Step.Target(targetType, UUID.randomUUID()))
			.withOrder(3)
			.withInputs(List.of(
				createFlowInputRef("input2"),
				createFlowInputRef("input3"),
				createRedirectedOutput("Step 1", "step1")));
	}

	public static StepInput createFlowInputRef(final String value) {
		return new FlowInputRef().withInput(value);
	}

	public static StepInput createRedirectedOutput(final String name, final String step) {
		return new RedirectedOutput().withUseAs(name).withStep(step);
	}

	public static FlowEntity createFlowEntity() {
		return new FlowEntity()
			.withId("a.flow")
			.withName("A flow")
			.withDescription("An example flow")
			.withVersion(1)
			.withContent("{}");
	}
}
