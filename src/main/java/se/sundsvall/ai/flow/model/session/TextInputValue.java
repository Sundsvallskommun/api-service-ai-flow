package se.sundsvall.ai.flow.model.session;

/** Domain text input value. */
public record TextInputValue(String name, String value)
	implements
	InputValue {}
