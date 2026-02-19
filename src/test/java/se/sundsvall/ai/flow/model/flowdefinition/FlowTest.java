package se.sundsvall.ai.flow.model.flowdefinition;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.zalando.problem.ThrowableProblem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.zalando.problem.Status.NOT_FOUND;

class FlowTest {

	@Test
	void setterAndGetter() {
		final var id = "id";
		final var name = "name";
		final var version = 123;
		final var description = "description";
		final var defaultTemplateId = "defaultTemplateId";
		final var helptext = "help text";
		final var spaceId = "space-123";
		final var visible = false;
		final var inputs = List.of(new FlowInput());
		final var step = new Step();
		step.setVisible(visible);
		final var steps = List.of(step);
		final var ttlInMinutes = 456;

		final var flow = new Flow();
		flow.setId(id);
		flow.setName(name);
		flow.setVersion(version);
		flow.setDescription(description);
		flow.setDefaultTemplateId(defaultTemplateId);
		flow.setTtlInMinutes(ttlInMinutes);
		flow.setHelptext(helptext);
		flow.setSpaceId(spaceId);
		flow.setFlowInputs(inputs);
		flow.setSteps(steps);

		assertThat(flow.getId()).isEqualTo(id);
		assertThat(flow.getName()).isEqualTo(name);
		assertThat(flow.getVersion()).isEqualTo(version);
		assertThat(flow.getDescription()).isEqualTo(description);
		assertThat(flow.getDefaultTemplateId()).isEqualTo(defaultTemplateId);
		assertThat(flow.getTtlInMinutes()).isEqualTo(ttlInMinutes);
		assertThat(flow.getFlowInputs()).isEqualTo(inputs);
		assertThat(flow.getSteps()).isEqualTo(steps);
		assertThat(flow.getHelptext()).isEqualTo(helptext);
		assertThat(flow.getSpaceId()).isEqualTo(spaceId);
		assertThat(flow.getSteps().get(0).isVisible()).isEqualTo(visible);
	}

	@Test
	void builderPattern() {
		final var id = "id";
		final var name = "name";
		final var version = 123;
		final var description = "description";
		final var ttlInMinutes = 456;
		final var defaultTemplateId = "defaultTemplateId";
		final var helptext = "help text";
		final var spaceId = "space-123";
		final var inputs = List.of(new FlowInput());
		final var steps = List.of(new Step());

		final var flow = new Flow()
			.withId(id)
			.withName(name)
			.withVersion(version)
			.withDescription(description)
			.withTtlInMinutes(ttlInMinutes)
			.withDefaultTemplateId(defaultTemplateId)
			.withFlowInputs(inputs)
			.withSteps(steps)
			.withHelptext(helptext)
			.withSpaceId(spaceId);

		assertThat(flow.getId()).isEqualTo(id);
		assertThat(flow.getName()).isEqualTo(name);
		assertThat(flow.getVersion()).isEqualTo(version);
		assertThat(flow.getDescription()).isEqualTo(description);
		assertThat(flow.getTtlInMinutes()).isEqualTo(ttlInMinutes);
		assertThat(flow.getDefaultTemplateId()).isEqualTo(defaultTemplateId);
		assertThat(flow.getFlowInputs()).isEqualTo(inputs);
		assertThat(flow.getSteps()).isEqualTo(steps);
		assertThat(flow.getHelptext()).isEqualTo(helptext);
		assertThat(flow.getSpaceId()).isEqualTo(spaceId);
	}

	@Test
	void getFlowInput() {
		final var id = "someId";

		final var flow = new Flow().withFlowInputs(List.of(new FlowInput().withId(id)));

		final var flowInput = flow.getFlowInput(id);

		assertThat(flowInput).isNotNull();
	}

	@Test
	void getFlowInputWhenMissing() {
		final var flowName = "someFlow";
		final var inputId = "unknownInput";

		final var flow = new Flow().withName(flowName);

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> flow.getFlowInput(inputId))
			.satisfies(thrownProblem -> {
				assertThat(thrownProblem.getStatus()).isEqualTo(NOT_FOUND);
				assertThat(thrownProblem.getMessage()).contains("No input '%s' exists in flow '%s'".formatted(inputId, flowName));
			});
	}

	@Test
	void getStep() {
		final var id = "someId";

		final var flow = new Flow().withSteps(List.of(new Step().withId(id)));

		final var step = flow.getStep(id);

		assertThat(step).isNotNull();
	}

	@Test
	void getStepWhenMissing() {
		final var flowName = "someFlow";
		final var stepId = "unknownStep";

		final var flow = new Flow().withName(flowName);

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> flow.getStep(stepId))
			.satisfies(thrownProblem -> {
				assertThat(thrownProblem.getStatus()).isEqualTo(NOT_FOUND);
				assertThat(thrownProblem.getMessage()).contains("No step '%s' exists in flow '%s'".formatted(stepId, flowName));
			});
	}
}
