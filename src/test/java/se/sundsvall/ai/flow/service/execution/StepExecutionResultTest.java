package se.sundsvall.ai.flow.service.execution;

import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StepExecutionResultTest {

	@Test
	void successFactoryCreatesSuccess() {
		final var runId = UUID.randomUUID();
		final var sessionId = UUID.randomUUID();

		final var result = StepExecutionResult.success("out", runId, sessionId);

		assertThat(result.success()).isTrue();
		assertThat(result.output()).isEqualTo("out");
		assertThat(result.runId()).isEqualTo(runId);
		assertThat(result.sessionId()).isEqualTo(sessionId);
		assertThat(result.errorMessage()).isNull();
	}

	@Test
	void failureFactoryCreatesFailure() {
		final var result = StepExecutionResult.failure("boom");

		assertThat(result.success()).isFalse();
		assertThat(result.output()).isNull();
		assertThat(result.runId()).isNull();
		assertThat(result.sessionId()).isNull();
		assertThat(result.errorMessage()).isEqualTo("boom");
	}
}
