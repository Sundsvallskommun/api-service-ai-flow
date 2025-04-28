package se.sundsvall.ai.flow.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ChatRequestTest {

	@Test
	void constructorAndGetter() {
		var input = "someInput";
		var runRequiredSteps = true;

		var chatRequest = new ChatRequest(input, runRequiredSteps);

		assertThat(chatRequest.input()).isEqualTo(input);
		assertThat(chatRequest.runRequiredSteps()).isEqualTo(runRequiredSteps);
	}
}
