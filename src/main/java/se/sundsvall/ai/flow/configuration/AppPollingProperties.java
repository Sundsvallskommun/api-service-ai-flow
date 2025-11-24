package se.sundsvall.ai.flow.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app-polling")
public record AppPollingProperties(
	@DefaultValue("2000") int intervalMs,

	@DefaultValue("300000") int maxDurationMs) {}
