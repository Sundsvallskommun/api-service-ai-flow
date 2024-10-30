package se.sundsvall.ai.flow.model.flow;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class FlowInputRef extends Input {

	@JsonProperty(FLOW_INPUT_REF)
	private String input;

	public FlowInputRef() {
		super(Type.FLOW_INPUT);
	}

	public String getInput() {
		return input;
	}

	public FlowInputRef withInput(final String input) {
		this.input = input;
		return this;
	}

	public void setInput(final String input) {
		this.input = input;
	}
}
