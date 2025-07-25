package se.sundsvall.ai.flow.model.session.exception;

import java.io.Serial;

public class ExecutionException extends RuntimeException {
	@Serial
	static final long serialVersionUID = -123432197878787L;

	public ExecutionException(final String message) {
		super(message);
	}

	public ExecutionException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
