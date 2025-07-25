package se.sundsvall.ai.flow.integration.intric;

import static java.time.Instant.now;
import static java.util.Optional.ofNullable;

import com.auth0.jwt.JWT;
import generated.intric.ai.AccessToken;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import se.sundsvall.ai.flow.integration.intric.configuration.IntricTokenConfiguration;

@Component
public class IntricTokenService {

	private final IntricTokenConfiguration intricTokenConfiguration;

	/**
	 * A cache for access tokens, the key is the municipality ID, and the value is the Token object. This avoids unnecessary
	 * requests for new tokens if a valid one is already available.
	 */
	private final Map<String, Token> intricTokens = new HashMap<>();

	public IntricTokenService(final IntricTokenConfiguration intricTokenConfiguration) {
		this.intricTokenConfiguration = intricTokenConfiguration;
	}

	/**
	 * Retrieves the access token for the given municipality ID.
	 *
	 * @param  municipalityId the ID of the municipality for which to retrieve the token
	 * @return                the access token as a String
	 */
	public String getToken(final String municipalityId) {
		// Get the cached token for the given municipality
		var token = intricTokens.get(municipalityId);
		// If we don't have a token at all, or if it's expired - get a new one
		if (token == null || token.accessToken() == null || (token.expiresAt() != null && token.expiresAt().isBefore(now()))) {
			var tokenResponse = retrieveToken(municipalityId);

			var accessToken = ofNullable(tokenResponse.getBody())
				.map(AccessToken::getAccessToken)
				.orElseThrow(() -> Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "Unable to extract access token"));

			// Decode the token to extract the expiresAt instant
			var jwt = JWT.decode(accessToken);
			var tokenExpiration = jwt.getExpiresAtAsInstant();

			intricTokens.put(municipalityId, new Token(accessToken, tokenExpiration));
		}

		return intricTokens.get(municipalityId).accessToken();
	}

	/**
	 * Retrieves the access token for the given municipality ID.
	 *
	 * @param  municipalityId the ID of the municipality for which to retrieve the token
	 * @return                a ResponseEntity containing the AccessToken
	 */
	ResponseEntity<AccessToken> retrieveToken(final String municipalityId) {
		return intricTokenConfiguration.getTokenClient(municipalityId).post()
			.body(intricTokenConfiguration.getTokenBody(municipalityId))
			.retrieve()
			.toEntity(AccessToken.class);
	}

	public record Token(String accessToken, Instant expiresAt) {
	}
}
