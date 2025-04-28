package se.sundsvall.ai.flow.model.flowdefinition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.zalando.problem.Status.NOT_FOUND;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.zalando.problem.ThrowableProblem;

class FlowTest {

	@Test
	void setterAndGetter() {
		var id = "id";
		var name = "name";
		var version = 123;
		var description = "description";
		var inputPrefix = "inputPrefix";
		var defaultTemplateId = "defaultTemplateId";
		var inputs = List.of(new FlowInput());
		var steps = List.of(new Step());

		var flow = new Flow();
		flow.setId(id);
		flow.setName(name);
		flow.setVersion(version);
		flow.setDescription(description);
		flow.setInputPrefix(inputPrefix);
		flow.setDefaultTemplateId(defaultTemplateId);
		flow.setFlowInputs(inputs);
		flow.setSteps(steps);

		assertThat(flow.getId()).isEqualTo(id);
		assertThat(flow.getName()).isEqualTo(name);
		assertThat(flow.getVersion()).isEqualTo(version);
		assertThat(flow.getDescription()).isEqualTo(description);
		assertThat(flow.getInputPrefix()).isEqualTo(inputPrefix);
		assertThat(flow.getDefaultTemplateId()).isEqualTo(defaultTemplateId);
		assertThat(flow.getFlowInputs()).isEqualTo(inputs);
		assertThat(flow.getSteps()).isEqualTo(steps);
	}

	@Test
	void builderPattern() {
		var id = "id";
		var name = "name";
		var version = 123;
		var description = "description";
		var inputPrefix = "inputPrefix";
		var ttlInMinutes = 456;
		var defaultTemplateId = "defaultTemplateId";
		var inputs = List.of(new FlowInput());
		var steps = List.of(new Step());

		var flow = new Flow()
			.withId(id)
			.withName(name)
			.withVersion(version)
			.withDescription(description)
			.withInputPrefix(inputPrefix)
			.withTtlInMinutes(ttlInMinutes)
			.withDefaultTemplateId(defaultTemplateId)
			.withFlowInputs(inputs)
			.withSteps(steps);

		assertThat(flow.getId()).isEqualTo(id);
		assertThat(flow.getName()).isEqualTo(name);
		assertThat(flow.getVersion()).isEqualTo(version);
		assertThat(flow.getDescription()).isEqualTo(description);
		assertThat(flow.getInputPrefix()).isEqualTo(inputPrefix);
		assertThat(flow.getTtlInMinutes()).isEqualTo(ttlInMinutes);
		assertThat(flow.getDefaultTemplateId()).isEqualTo(defaultTemplateId);
		assertThat(flow.getFlowInputs()).isEqualTo(inputs);
		assertThat(flow.getSteps()).isEqualTo(steps);
	}

	@Test
	void getFlowInput() {
		var id = "someId";

		var flow = new Flow().withFlowInputs(List.of(new FlowInput().withId(id)));

		var flowInput = flow.getFlowInput(id);

		assertThat(flowInput).isNotNull();
	}

	@Test
	void getFlowInputWhenMissing() {
		var flowName = "someFlow";
		var inputId = "unknownInput";

		var flow = new Flow().withName(flowName);

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> flow.getFlowInput(inputId))
			.satisfies(thrownProblem -> {
				assertThat(thrownProblem.getStatus()).isEqualTo(NOT_FOUND);
				assertThat(thrownProblem.getMessage()).contains("No input '%s' exists in flow '%s'".formatted(inputId, flowName));
			});
	}

	@Test
	void getStep() {
		var id = "someId";

		var flow = new Flow().withSteps(List.of(new Step().withId(id)));

		var step = flow.getStep(id);

		assertThat(step).isNotNull();
	}

	@Test
	void getStepWhenMissing() {
		var flowName = "someFlow";
		var stepId = "unknownStep";

		var flow = new Flow().withName(flowName);

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> flow.getStep(stepId))
			.satisfies(thrownProblem -> {
				assertThat(thrownProblem.getStatus()).isEqualTo(NOT_FOUND);
				assertThat(thrownProblem.getMessage()).contains("No step '%s' exists in flow '%s'".formatted(stepId, flowName));
			});
	}
}
