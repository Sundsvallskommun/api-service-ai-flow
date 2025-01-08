package se.sundsvall.ai.flow.integration.intric.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RunServiceTest {

	@Test
	void constructorAndGetter() {
		var input = "input";

		var object = new RunService(input);

		assertThat(object.input()).isEqualTo(input);
	}

}
