package se.sundsvall.ai.flow.integration.intric.util;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "properties.credentials")
public record CredentialsProperties(String secretKey) {
}
