package se.sundsvall.ai.flow.integration.eneo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "integration.eneo")
public record EneoProperties(

	@Valid @NotNull Oauth2 oauth2,

	@DefaultValue("5") int connectTimeoutInSeconds,

	@DefaultValue("30") int readTimeoutInSeconds,

	Map<String, MunicipalityConfig> municipalities) {

	public record Oauth2(
		@NotBlank String tokenUrl,
		@NotBlank String clientId,
		@NotBlank String clientSecret,
		@DefaultValue("client_credentials") String authorizationGrantType) {
	}

	record MunicipalityConfig(String url, String apiKey) {
	}
}
