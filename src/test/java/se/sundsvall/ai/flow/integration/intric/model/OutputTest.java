package se.sundsvall.ai.flow.integration.intric.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class OutputTest {

	@Test
	void constructorAndGetter() {
		var output = "output";

		var object = new Output(output);

		assertThat(object.output()).isEqualTo(output);
	}

}
