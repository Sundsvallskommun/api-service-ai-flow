package se.sundsvall.ai.flow.configuration;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app-polling")
public record AppPollingProperties(
	@DefaultValue("PT2S") Duration interval,

	@DefaultValue("PT30S") Duration maxDuration) {
}
