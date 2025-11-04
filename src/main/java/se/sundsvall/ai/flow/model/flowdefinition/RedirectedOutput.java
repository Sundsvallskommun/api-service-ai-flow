package se.sundsvall.ai.flow.model.flowdefinition;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({
	"use-output-from-step", "use-as"
})
public final class RedirectedOutput extends StepInput {

	@JsonProperty("use-output-from-step")
	private String step;
	@JsonProperty("use-as")
	private String useAs;

	public RedirectedOutput() {
		super(Type.STEP_OUTPUT);
	}

	public String getStep() {
		return step;
	}

	public RedirectedOutput withStep(final String step) {
		this.step = step;
		return this;
	}

	public void setStep(final String step) {
		this.step = step;
	}

	public String getUseAs() {
		return useAs;
	}

	public RedirectedOutput withUseAs(final String useAs) {
		this.useAs = useAs;
		return this;
	}

	public void setUseAs(final String useAs) {
		this.useAs = useAs;
	}
}
