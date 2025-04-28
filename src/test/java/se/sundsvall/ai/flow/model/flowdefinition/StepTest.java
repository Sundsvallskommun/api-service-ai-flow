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
		var id = "id";
		var order = 1;
		var name = "name";
		var description = "description";
		var intricEndpoint = new Step.IntricEndpoint(Step.IntricEndpoint.Type.ASSISTANT, UUID.randomUUID());
		var inputs = List.of(createFlowInputRef("value"));

		var step = new Step();

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
		var id = "id";
		var order = 1;
		var name = "name";
		var description = "description";
		var intricEndpointType = Step.IntricEndpoint.Type.ASSISTANT;
		var intricEndpointId = UUID.randomUUID();
		var inputs = List.of(createFlowInputRef("value"));

		var step = new Step()
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
		assertThat(step.getIntricEndpoint()).satisfies(intricEndpoint -> {
			assertThat(intricEndpoint.type()).isEqualTo(intricEndpointType);
			assertThat(intricEndpoint.id()).isEqualTo(intricEndpointId);
		});
		assertThat(step.getInputs()).isEqualTo(inputs);
	}

	@Test
	void compareTo() {
		var step1 = new Step().withOrder(5);
		var step2 = new Step().withOrder(2);
		var step3 = new Step().withOrder(7);

		assertThat(step1).isGreaterThan(step2).isLessThan(step3);
		assertThat(step2).isLessThan(step3);
	}

	@Nested
	class IntricEndpointTest {

		@Test
		void constructorAndGetters() {
			var type = Step.IntricEndpoint.Type.ASSISTANT;
			var endpointId = UUID.randomUUID();

			var intricEndpoint = new Step.IntricEndpoint(type, endpointId);

			assertThat(intricEndpoint.type()).isEqualTo(type);
			assertThat(intricEndpoint.id()).isEqualTo(endpointId);
		}
	}
}
