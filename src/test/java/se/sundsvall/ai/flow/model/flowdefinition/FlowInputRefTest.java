package se.sundsvall.ai.flow.model.flowdefinition;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FlowInputRefTest {

	@Test
	void setterAndGetter() {
		var input = "input";

		var flowInputRef = new FlowInputRef();
		flowInputRef.setInput(input);

		assertThat(flowInputRef.getInput()).isEqualTo(input);
		assertThat(flowInputRef.getType()).isEqualTo(StepInput.Type.FLOW_INPUT);
	}

	@Test
	void builderPattern() {
		var input = "input";

		var flowInputRef = new FlowInputRef().withInput(input);

		assertThat(flowInputRef.getInput()).isEqualTo(input);
	}
}
