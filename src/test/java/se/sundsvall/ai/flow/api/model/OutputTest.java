package se.sundsvall.ai.flow.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class OutputTest {

	@Test
	void constructorAndGetter() {
		var data = "data";

		var output = new Output(data);

		assertThat(output.data()).isEqualTo(data);
		assertThat(output).hasNoNullFieldsOrProperties();
		assertThat(output).hasOnlyFields("data");
	}
}
