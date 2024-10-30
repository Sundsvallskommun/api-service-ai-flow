package se.sundsvall.ai.flow.service.flow.exception;

public class FlowExecutionException extends FlowException {

	public FlowExecutionException(final String message) {
		super(message);
	}

	public FlowExecutionException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
