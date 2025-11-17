package se.sundsvall.ai.flow.model.flowdefinition;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.ai.flow.TestDataFactory.createFlowInputRef;
import static se.sundsvall.ai.flow.model.flowdefinition.Step.IntricEndpoint.Type.APP;
import static se.sundsvall.ai.flow.model.flowdefinition.Step.IntricEndpoint.Type.ASSISTANT;
import static se.sundsvall.ai.flow.model.flowdefinition.Step.IntricEndpoint.Type.SERVICE;
import static se.sundsvall.ai.flow.model.flowdefinition.Step.IntricEndpoint.Type.values;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import se.sundsvall.ai.flow.model.flowdefinition.Step.IntricEndpoint.Type;

class StepTest {

	@Test
	void getterAndSetter() {
		final var id = "id";
		final var order = 1;
		final var name = "name";
		final var description = "description";
		final var intricEndpoint = new Step.IntricEndpoint(ASSISTANT, UUID.randomUUID());
		final var inputs = List.of(createFlowInputRef("value"));

		final var step = new Step();

		step.setId(id);
		step.setOrder(order);
		step.setName(name);
		step.setDescription(description);
		step.setIntricEndpoint(intricEndpoint);
		step.setInputs(inputs);

		assertThat(step.getId()).isEqualTo(id);
		assertThat(step.getOrder()).isEqualTo(order);
		assertThat(step.getName()).isEqualTo(name);
		assertThat(step.getDescription()).isEqualTo(description);
		assertThat(step.getIntricEndpoint()).isEqualTo(intricEndpoint);
		assertThat(step.getInputs()).isEqualTo(inputs);
	}

	@Test
	void builderPattern() {
		final var id = "id";
		final var order = 1;
		final var name = "name";
		final var description = "description";
		final var intricEndpointType = ASSISTANT;
		final var intricEndpointId = UUID.randomUUID();
		final var inputs = List.of(createFlowInputRef("value"));

		final var step = new Step()
			.withId(id)
			.withOrder(order)
			.withName(name)
			.withDescription(description)
			.withIntricEndpoint(new Step.IntricEndpoint(intricEndpointType, intricEndpointId))
			.withInputs(inputs);

		assertThat(step.getId()).isEqualTo(id);
		assertThat(step.getOrder()).isEqualTo(order);
		assertThat(step.getName()).isEqualTo(name);
		assertThat(step.getDescription()).isEqualTo(description);
		assertThat(step.getIntricEndpoint()).satisfies(eneoEndpoint -> {
			assertThat(eneoEndpoint.type()).isEqualTo(intricEndpointType);
			assertThat(eneoEndpoint.id()).isEqualTo(intricEndpointId);
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

		@EnumSource(value = Type.class)
		@ParameterizedTest
		void testConstructorAndGetters(final Type type) {
			final var endpointId = UUID.randomUUID();

			final var intricEndpoint = new Step.IntricEndpoint(type, endpointId);

			assertThat(intricEndpoint.type()).isEqualTo(type);
			assertThat(intricEndpoint.id()).isEqualTo(endpointId);
			assertThat(intricEndpoint).hasNoNullFieldsOrProperties();
		}

		@Test
		void testAllEndpointTypes() {
			assertThat(values()).hasSize(3)
				.containsExactlyInAnyOrder(
					SERVICE,
					ASSISTANT,
					APP);
		}
	}
}
