package se.sundsvall.ai.flow.model.flowdefinition.exception;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;

class FlowConfigurationExceptionTest {

	@Test
	void constructorWithMessage() {
		var message = "someMessage";

		assertThatExceptionOfType(FlowConfigurationException.class)
			.isThrownBy(() -> { throw new FlowConfigurationException(message); })
			.withMessage(message);
	}

	@Test
	void constructorWithMessageAndCause() {
		var message = "someMessage";
		var cause = new RuntimeException("someCauseMessage");

		assertThatExceptionOfType(FlowConfigurationException.class)
			.isThrownBy(() -> { throw new FlowConfigurationException(message, cause); })
			.withMessage(message)
			.withCause(cause);
	}
}
