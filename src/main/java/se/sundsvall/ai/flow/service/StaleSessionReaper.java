package se.sundsvall.ai.flow.service;

import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import se.sundsvall.dept44.scheduling.Dept44Scheduled;

@Component
@ConditionalOnProperty(value = "stale-session-reaper.enabled", havingValue = "true", matchIfMissing = true)
class StaleSessionReaper {

	private static final Logger LOG = LoggerFactory.getLogger(StaleSessionReaper.class);

	private final SessionService sessionService;

	StaleSessionReaper(final SessionService sessionService) {
		this.sessionService = sessionService;
	}

	@Dept44Scheduled(cron = "${scheduler.stale-session-reaper.cron.expression}",
		name = "${scheduler.stale-session-reaper.name}",
		lockAtMostFor = "${scheduler.stale-session-reaper.shedlock-lock-at-most-for}",
		maximumExecutionTime = "${scheduler.stale-session-reaper.maximum-execution-time}")
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
				sessionService.deleteSession(session.getMunicipalityId(), session.getId());

				LOG.info("Deleted stale session {}", session.getId());
			}
		});
	}
}
