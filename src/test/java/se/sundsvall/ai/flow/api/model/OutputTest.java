package se.sundsvall.ai.flow.api.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OutputTest {

	@Test
	void constructorAndGetter() {
		var data = "data";

		var output = new Output(data);

		assertThat(output.data()).isEqualTo(data);
		assertThat(output).hasNoNullFieldsOrProperties().hasOnlyFields("data");
	}
}
