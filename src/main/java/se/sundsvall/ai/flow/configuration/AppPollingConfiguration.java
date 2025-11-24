package se.sundsvall.ai.flow.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AppPollingProperties.class)
class AppPollingConfiguration {
}
