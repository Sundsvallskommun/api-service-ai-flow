package se.sundsvall.ai.flow.service;

import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "stale-session-reaper.enabled", havingValue = "true", matchIfMissing = true)
class StaleSessionReaper {

	private static final Logger LOG = LoggerFactory.getLogger(StaleSessionReaper.class);

	private final SessionService sessionService;

	StaleSessionReaper(final SessionService sessionService) {
		this.sessionService = sessionService;
	}

	@Scheduled(initialDelayString = "${stale-session-reaper.check-interval:PT3H}", fixedRateString = "${stale-session-reaper.check-interval:PT3H}")
	public void run() {
		var sessions = sessionService.getAllSessions();
		if (sessions.isEmpty()) {
			return;
		}

		sessions.forEach(session -> {
			var now = LocalDateTime.now();
			var lastUpdatedAt = session.getLastUpdatedAt();
			var ttlInMinutes = session.getFlow().getTtlInMinutes();

			if (lastUpdatedAt.plusMinutes(ttlInMinutes).isBefore(now)) {
				sessionService.deleteSession(session.getId());

				LOG.info("Deleted stale session {}", session.getId());
			}
		});
	}
}
