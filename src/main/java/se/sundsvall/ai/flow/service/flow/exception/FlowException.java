package se.sundsvall.ai.flow.service.flow.exception;

public class FlowException extends RuntimeException {

    public FlowException(final String message) {
        super(message);
    }

    public FlowException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
