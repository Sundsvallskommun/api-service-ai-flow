package se.sundsvall.ai.flow.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class InputTest {

	@Test
	void constructorAndGetter() {
		var inputId = "inputId";
		var value = "value";

		var input = new Input(inputId, value);

		assertThat(input.inputId()).isEqualTo(inputId);
		assertThat(input.value()).isEqualTo(value);
		assertThat(input).hasNoNullFieldsOrProperties().hasOnlyFields("inputId", "value");
	}
}
