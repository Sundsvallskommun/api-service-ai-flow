package se.sundsvall.ai.flow.model.flowdefinition;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class FlowInputRef extends StepInput {

	@JsonProperty("use-flow-input")
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
