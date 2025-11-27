package se.sundsvall.ai.flow.model.session;

/** Marker interface for domain input values stored in a Session. */
public sealed interface InputValue permits TextInputValue, FileInputValue {
}
