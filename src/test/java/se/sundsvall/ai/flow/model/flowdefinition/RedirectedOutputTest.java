package se.sundsvall.ai.flow.model.flowdefinition;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RedirectedOutputTest {

	@Test
	void getterAndSetters() {
		final var step = "step";
		final var name = "name";

		final var redirectedOutput = new RedirectedOutput();
		redirectedOutput.setStep(step);
		redirectedOutput.setUseAs(name);

		assertThat(redirectedOutput.getStep()).isEqualTo(step);
		assertThat(redirectedOutput.getUseAs()).isEqualTo(name);
		assertThat(redirectedOutput.getType()).isEqualTo(StepInput.Type.STEP_OUTPUT);
	}

	@Test
	void builderPattern() {
		final var step = "step";
		final var name = "name";

		final var redirectedOutput = new RedirectedOutput()
			.withStep(step)
			.withUseAs(name);

		assertThat(redirectedOutput.getStep()).isEqualTo(step);
		assertThat(redirectedOutput.getUseAs()).isEqualTo(name);
		assertThat(redirectedOutput.getType()).isEqualTo(StepInput.Type.STEP_OUTPUT);
	}
}
