package se.sundsvall.ai.flow.configuration;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "stale-session-reaper")
public record StaleSessionReaperProperties(
	@DefaultValue("true") boolean enabled,

	@DefaultValue("PT5M") Duration checkInterval,

	@DefaultValue("5") int threadPoolSize) {}
