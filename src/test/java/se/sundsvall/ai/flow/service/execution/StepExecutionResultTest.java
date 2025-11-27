package se.sundsvall.ai.flow.service.execution;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class StepExecutionResultTest {

	@Test
	void successFactoryCreatesSuccess() {
		final var runId = UUID.randomUUID();
		final var sessionId = UUID.randomUUID();

		final var r = StepExecutionResult.success("out", runId, sessionId);

		assertThat(r.success()).isTrue();
		assertThat(r.output()).isEqualTo("out");
		assertThat(r.runId()).isEqualTo(runId);
		assertThat(r.sessionId()).isEqualTo(sessionId);
		assertThat(r.errorMessage()).isNull();
	}

	@Test
	void failureFactoryCreatesFailure() {
		final var r = StepExecutionResult.failure("boom");

		assertThat(r.success()).isFalse();
		assertThat(r.output()).isNull();
		assertThat(r.runId()).isNull();
		assertThat(r.sessionId()).isNull();
		assertThat(r.errorMessage()).isEqualTo("boom");
	}
}
