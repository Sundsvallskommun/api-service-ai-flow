package se.sundsvall.ai.flow.api.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleInputTest {

	@Test
	void constructorAndGetter() {
		var value = "someValue";

		var simpleInput = new SimpleInput(value);

		assertThat(simpleInput.value()).isEqualTo(value);
	}
}
