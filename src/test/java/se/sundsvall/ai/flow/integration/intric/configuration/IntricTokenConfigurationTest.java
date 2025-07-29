package se.sundsvall.ai.flow.integration.intric.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import se.sundsvall.ai.flow.integration.db.InstanceRepository;
import se.sundsvall.ai.flow.integration.db.model.InstanceEntity;
import se.sundsvall.ai.flow.util.EncryptionUtility;

@ExtendWith(MockitoExtension.class)
class IntricTokenConfigurationTest {

	private static final String MUNICIPALITY_ID = "2281";

	@Mock
	private InstanceRepository instanceRepositoryMock;

	@Mock
	private EncryptionUtility encryptionUtilityMock;

	@InjectMocks
	private IntricTokenConfiguration intricTokenConfiguration;

	@Test
	void constructorTest() {
		var instanceEntity = new InstanceEntity()
			.withTokenUrl("https://example.com/token")
			.withMunicipalityId(MUNICIPALITY_ID);
		when(instanceRepositoryMock.findAll()).thenReturn(List.of(instanceEntity));

		intricTokenConfiguration = new IntricTokenConfiguration(instanceRepositoryMock, encryptionUtilityMock);

		assertThat(intricTokenConfiguration.getTokenBody(MUNICIPALITY_ID)).isNotEmpty();
		assertThat(intricTokenConfiguration.getTokenClient(MUNICIPALITY_ID)).isNotNull();
	}

	@Test
	void getTokenClient() {
		Map<String, RestClient> intricClients = new HashMap<>();
		var restClientMock = mock(RestClient.class);
		intricClients.put(MUNICIPALITY_ID, restClientMock);
		ReflectionTestUtils.setField(intricTokenConfiguration, "intricTokenClients", intricClients);

		var result = intricTokenConfiguration.getTokenClient(MUNICIPALITY_ID);

		assertThat(result).isEqualTo(restClientMock);
	}

	@Test
	void getTokenBody() {
		Map<String, LinkedMultiValueMap<String, String>> intricTokenBodies = new HashMap<>();
		var intricTokens = new LinkedMultiValueMap<String, String>();
		intricTokenBodies.put(MUNICIPALITY_ID, intricTokens);
		ReflectionTestUtils.setField(intricTokenConfiguration, "intricTokenBodies", intricTokenBodies);

		var result = intricTokenConfiguration.getTokenBody(MUNICIPALITY_ID);

		assertThat(result).isEqualTo(intricTokens);
	}

}
