package se.sundsvall.ai.flow.model.flowdefinition.exception;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;

class FlowExceptionTest {

	@Test
	void constructorWithMessage() {
		var message = "someMessage";

		assertThatExceptionOfType(FlowException.class)
			.isThrownBy(() -> { throw new FlowException(message); })
			.withMessage(message);
	}

	@Test
	void constructorWithMessageAndCause() {
		var message = "someMessage";
		var cause = new RuntimeException("someCauseMessage");

		assertThatExceptionOfType(FlowException.class)
			.isThrownBy(() -> { throw new FlowException(message, cause); })
			.withMessage(message)
			.withCause(cause);
	}
}
