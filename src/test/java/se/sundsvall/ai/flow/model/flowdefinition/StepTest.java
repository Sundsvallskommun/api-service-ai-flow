package se.sundsvall.ai.flow.model.flowdefinition;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.ai.flow.TestDataFactory.createFlowInputRef;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class StepTest {

	@Test
	void getterAndSetter() {
		final var id = "id";
		final var order = 1;
		final var name = "name";
		final var description = "description";
		final var target = new Step.Target(Step.Target.Type.ASSISTANT, UUID.randomUUID());
		final var inputs = List.of(createFlowInputRef("value"));

		final var step = new Step();

		step.setId(id);
		step.setOrder(order);
		step.setName(name);
		step.setDescription(description);
		step.setTarget(target);
		step.setInputs(inputs);

		assertThat(step.getId()).isEqualTo(id);
		assertThat(step.getOrder()).isEqualTo(order);
		assertThat(step.getName()).isEqualTo(name);
		assertThat(step.getDescription()).isEqualTo(description);
		assertThat(step.getTarget()).isEqualTo(target);
		assertThat(step.getInputs()).isEqualTo(inputs);
	}

	@Test
	void builderPattern() {
		final var id = "id";
		final var order = 1;
		final var name = "name";
		final var description = "description";
		final var targetType = Step.Target.Type.ASSISTANT;
		final var targetId = UUID.randomUUID();
		final var inputs = List.of(createFlowInputRef("value"));

		final var step = new Step()
			.withId(id)
			.withOrder(order)
			.withName(name)
			.withDescription(description)
			.withTarget(new Step.Target(targetType, targetId))
			.withInputs(inputs);

		assertThat(step.getId()).isEqualTo(id);
		assertThat(step.getOrder()).isEqualTo(order);
		assertThat(step.getName()).isEqualTo(name);
		assertThat(step.getDescription()).isEqualTo(description);
		assertThat(step.getTarget()).satisfies(target -> {
			assertThat(target.type()).isEqualTo(targetType);
			assertThat(target.id()).isEqualTo(targetId);
		});
		assertThat(step.getInputs()).isEqualTo(inputs);
	}

	@Test
	void compareTo() {
		final var step1 = new Step().withOrder(5);
		final var step2 = new Step().withOrder(2);
		final var step3 = new Step().withOrder(7);

		assertThat(step1).isGreaterThan(step2).isLessThan(step3);
		assertThat(step2).isLessThan(step3);
	}

	@Nested
	class EneoEndpointTest {

		@Test
		void constructorAndGetters() {
			final var type = Step.Target.Type.ASSISTANT;
			final var endpointId = UUID.randomUUID();

			final var target = new Step.Target(type, endpointId);

			assertThat(target.type()).isEqualTo(type);
			assertThat(target.id()).isEqualTo(endpointId);
		}
	}
}
