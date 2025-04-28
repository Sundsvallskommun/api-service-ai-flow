package se.sundsvall.ai.flow.model.session.exception;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;

class ExecutionExceptionTest {

	@Test
	void constructorWithMessage() {
		var message = "someMessage";

		assertThatExceptionOfType(ExecutionException.class)
			.isThrownBy(() -> { throw new ExecutionException(message); })
			.withMessage(message);
	}

	@Test
	void constructorWithMessageAndCause() {
		var message = "someMessage";
		var cause = new RuntimeException("someCauseMessage");

		assertThatExceptionOfType(ExecutionException.class)
			.isThrownBy(() -> { throw new ExecutionException(message, cause); })
			.withMessage(message)
			.withCause(cause);
	}
}
