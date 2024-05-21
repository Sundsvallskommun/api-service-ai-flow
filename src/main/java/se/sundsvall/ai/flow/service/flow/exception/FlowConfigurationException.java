package se.sundsvall.ai.flow.service.flow.exception;

public class FlowConfigurationException extends FlowException {

    public FlowConfigurationException(final String message) {
        super(message);
    }

    public FlowConfigurationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
