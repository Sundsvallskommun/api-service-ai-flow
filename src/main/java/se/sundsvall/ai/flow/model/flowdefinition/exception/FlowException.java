package se.sundsvall.ai.flow.model.flowdefinition.exception;

import java.io.Serial;

public class FlowException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = -703432197126693L;

	public FlowException(final String message) {
		super(message);
	}

	public FlowException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
