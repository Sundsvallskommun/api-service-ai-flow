package se.sundsvall.ai.flow.model.flowdefinition.exception;

import java.io.Serial;

public class FlowConfigurationException extends FlowException {
	@Serial
	static final long serialVersionUID = -7321321197126693L;

	public FlowConfigurationException(final String message) {
		super(message);
	}

	public FlowConfigurationException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
