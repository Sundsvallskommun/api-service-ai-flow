package se.sundsvall.ai.flow.integration.templating;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "integration.templating")
record TemplatingIntegrationProperties(

	@NotBlank String baseUrl,

	String apiKey,

	@Valid @NotNull Oauth2 oauth2,

	@DefaultValue("10") int connectTimeoutInSeconds,

	@DefaultValue("30") int readTimeoutInSeconds) {

	record Oauth2(

		@NotBlank String tokenUrl,
		@NotBlank String clientId,
		@NotBlank String clientSecret,
		@DefaultValue("client_credentials") String authorizationGrantType) {}
}
