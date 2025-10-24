package se.sundsvall.ai.flow.integration.eneo.util;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "properties.credentials")
public record CredentialsProperties(String secretKey) {
}
