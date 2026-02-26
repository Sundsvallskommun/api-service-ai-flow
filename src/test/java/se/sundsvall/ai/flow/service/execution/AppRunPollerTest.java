package se.sundsvall.ai.flow.service.execution;

import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import se.sundsvall.ai.flow.configuration.AppPollingProperties;
import se.sundsvall.ai.flow.integration.eneo.EneoService;
import se.sundsvall.ai.flow.integration.eneo.model.Response;
import se.sundsvall.dept44.problem.Problem;

import static generated.eneo.ai.Status.COMPLETE;
import static generated.eneo.ai.Status.FAILED;
import static generated.eneo.ai.Status.IN_PROGRESS;
import static generated.eneo.ai.Status.NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

class AppRunPollerTest {

	@Test
	void completesSuccessfully() throws Exception {
		final var eneo = Mockito.mock(EneoService.class);
		final var props = new AppPollingProperties(Duration.ofMillis(1), Duration.ofMillis(50));
		final var poller = new AppRunPoller(eneo, props);
		final var runId = UUID.randomUUID();

		// First poll returns IN_PROGRESS, second returns COMPLETE with answer
		when(eneo.getAppRun("2281", runId))
			.thenReturn(new Response(runId, null, IN_PROGRESS))
			.thenReturn(new Response(runId, "done", COMPLETE));

		final var out = poller.pollUntilComplete("2281", runId, "step");
		assertThat(out).isEqualTo("done");
	}

	@Test
	void failsWithProblemOnFailed() {
		final var eneo = Mockito.mock(EneoService.class);
		final var props = new AppPollingProperties(Duration.ofMillis(1), Duration.ofMillis(20));
		final var poller = new AppRunPoller(eneo, props);
		final var runId = UUID.randomUUID();

		when(eneo.getAppRun("2281", runId))
			.thenReturn(new Response(runId, null, FAILED));

		assertThatThrownBy(() -> poller.pollUntilComplete("2281", runId, "step"))
			.isInstanceOf(Problem.class)
			.hasMessageContaining("failed");
	}

	@Test
	void failsWithProblemOnNotFound() {
		final var eneo = Mockito.mock(EneoService.class);
		final var props = new AppPollingProperties(Duration.ofMillis(1), Duration.ofMillis(20));
		final var poller = new AppRunPoller(eneo, props);
		final var runId = UUID.randomUUID();

		when(eneo.getAppRun("2281", runId))
			.thenReturn(new Response(runId, null, NOT_FOUND));

		assertThatThrownBy(() -> poller.pollUntilComplete("2281", runId, "step"))
			.isInstanceOf(Problem.class)
			.hasMessageContaining("not found");
	}
}
