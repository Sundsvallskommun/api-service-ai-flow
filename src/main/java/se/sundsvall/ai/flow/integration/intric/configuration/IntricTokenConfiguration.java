package se.sundsvall.ai.flow.integration.intric.configuration;

import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.zalando.problem.Problem;
import se.sundsvall.ai.flow.integration.db.InstanceRepository;
import se.sundsvall.ai.flow.integration.db.model.InstanceEntity;
import se.sundsvall.ai.flow.util.EncryptionUtility;

@Component
public class IntricTokenConfiguration {

	/**
	 * The key is the municipality ID, and the value is the municipality specific configured RestClient for token retrieval.
	 */
	private final Map<String, RestClient> intricTokenClients = new HashMap<>();

	/**
	 * The key is the municipality ID, and the value is the municipality specific configured access token body.
	 */
	private final Map<String, LinkedMultiValueMap<String, String>> intricTokenBodies = new HashMap<>();

	public IntricTokenConfiguration(final InstanceRepository instanceRepository, final EncryptionUtility encryptionUtility) {
		instanceRepository.findAll().forEach(instanceEntity -> {
			createTokenClient(instanceEntity);
			createAccessTokenBody(instanceEntity, encryptionUtility);
		});
	}

	private void createTokenClient(final InstanceEntity instanceEntity) {
		var tokenClient = RestClient.builder()
			.baseUrl(instanceEntity.getTokenUrl())
			.defaultHeader(ACCEPT, APPLICATION_JSON_VALUE)
			.defaultHeader(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE)
			.defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
				throw Problem.valueOf(INTERNAL_SERVER_ERROR,
					"Unable to retrieve access token for instance '%s'".formatted(instanceEntity.getMunicipalityId()));
			})
			.build();

		intricTokenClients.put(instanceEntity.getMunicipalityId(), tokenClient);
	}

	private void createAccessTokenBody(final InstanceEntity instanceEntity, final EncryptionUtility encryptionUtility) {
		var accessTokenRequestData = new LinkedMultiValueMap<String, String>();
		accessTokenRequestData.add("grant_type", "password");
		accessTokenRequestData.add("username", instanceEntity.getUsername());
		accessTokenRequestData.add("password", encryptionUtility.decrypt(instanceEntity.getPassword()));
		accessTokenRequestData.add("scope", "");
		accessTokenRequestData.add("client_id", "");
		accessTokenRequestData.add("client_secret", "");

		intricTokenBodies.put(instanceEntity.getMunicipalityId(), accessTokenRequestData);
	}

	public RestClient getTokenClient(final String municipalityId) {
		return intricTokenClients.get(municipalityId);
	}

	public LinkedMultiValueMap<String, String> getTokenBody(final String municipalityId) {
		return intricTokenBodies.get(municipalityId);
	}
}
