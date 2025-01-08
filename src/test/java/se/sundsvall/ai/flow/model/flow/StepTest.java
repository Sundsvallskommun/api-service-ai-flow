package se.sundsvall.ai.flow.model.flow;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.ai.flow.TestDataFactory.createFlowInputRef;

import java.util.List;
import org.junit.jupiter.api.Test;

class StepTest {

	@Test
	void getterAndSetter() {
		var id = "id";
		var order = 1;
		var name = "name";
		var description = "description";
		var intricServiceId = "intricServiceId";
		List<Input> inputs = List.of(createFlowInputRef("value"));

		var step = new Step();

		step.setId(id);
		step.setOrder(order);
		step.setName(name);
		step.setDescription(description);
		step.setIntricServiceId(intricServiceId);
		step.setInputs(inputs);

		assertThat(step.getId()).isEqualTo(id);
		assertThat(step.getOrder()).isEqualTo(order);
		assertThat(step.getName()).isEqualTo(name);
		assertThat(step.getDescription()).isEqualTo(description);
		assertThat(step.getIntricServiceId()).isEqualTo(intricServiceId);
		assertThat(step.getInputs()).isEqualTo(inputs);
	}

	@Test
	void builderPattern() {
		var id = "id";
		var order = 1;
		var name = "name";
		var description = "description";
		var intricServiceId = "intricServiceId";
		List<Input> inputs = List.of(createFlowInputRef("value"));

		var step = new Step();

		step.withId(id)
			.withOrder(order)
			.withName(name)
			.withDescription(description)
			.withIntricServiceId(intricServiceId)
			.withInputs(inputs);

		assertThat(step.getId()).isEqualTo(id);
		assertThat(step.getOrder()).isEqualTo(order);
		assertThat(step.getName()).isEqualTo(name);
		assertThat(step.getDescription()).isEqualTo(description);
		assertThat(step.getIntricServiceId()).isEqualTo(intricServiceId);
		assertThat(step.getInputs()).isEqualTo(inputs);
	}
}
