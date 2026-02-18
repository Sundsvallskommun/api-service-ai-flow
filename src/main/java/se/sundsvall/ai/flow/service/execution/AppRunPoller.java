package se.sundsvall.ai.flow.service.execution;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import se.sundsvall.ai.flow.configuration.AppPollingProperties;
import se.sundsvall.ai.flow.integration.eneo.EneoService;

import static generated.eneo.ai.Status.COMPLETE;
import static generated.eneo.ai.Status.FAILED;
import static generated.eneo.ai.Status.NOT_FOUND;

@Component
public class AppRunPoller {
	private static final Logger LOG = LoggerFactory.getLogger(AppRunPoller.class);

	private final EneoService eneoService;
	private final AppPollingProperties appPollingProperties;

	public AppRunPoller(final EneoService eneoService, final AppPollingProperties appPollingProperties) {
		this.eneoService = eneoService;
		this.appPollingProperties = appPollingProperties;
	}

	public String pollUntilComplete(final String municipalityId, final UUID runId, final String stepName) throws InterruptedException {
		final var pollIntervalMs = appPollingProperties.interval().toMillis();
		final var maxPollDurationMs = appPollingProperties.maxDuration().toMillis();
		final long startTime = System.currentTimeMillis();

		generated.eneo.ai.Status currentStatus;
		String output;

		while (true) {
			if (System.currentTimeMillis() - startTime > maxPollDurationMs) {
				throw Problem.valueOf(Status.GATEWAY_TIMEOUT, "App run for step '%s' timed out after %s ms".formatted(stepName, maxPollDurationMs));
			}

			LOG.debug("Polling app run status for step {} with run ID {}", stepName, runId);
			final var currentResponse = eneoService.getAppRun(municipalityId, runId);
			currentStatus = currentResponse.status();
			output = currentResponse.answer();

			if (currentStatus == COMPLETE || currentStatus == FAILED || currentStatus == NOT_FOUND) {
				break;
			}

			TimeUnit.MILLISECONDS.sleep(pollIntervalMs);
		}

		if (currentStatus == FAILED) {
			throw Problem.valueOf(Status.BAD_GATEWAY, "App run for step '%s' failed".formatted(stepName));
		}
		if (currentStatus == NOT_FOUND) {
			throw Problem.valueOf(Status.NOT_FOUND, "App run for step '%s' not found".formatted(stepName));
		}

		LOG.info("App run for step {} completed successfully", stepName);
		return output;
	}
}
