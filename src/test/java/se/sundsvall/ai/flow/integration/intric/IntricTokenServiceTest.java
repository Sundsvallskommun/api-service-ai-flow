package se.sundsvall.ai.flow.integration.intric;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import generated.intric.ai.AccessToken;
import java.time.Instant;
import java.util.HashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import se.sundsvall.ai.flow.integration.intric.configuration.IntricTokenConfiguration;

@ExtendWith(MockitoExtension.class)
class IntricTokenServiceTest {

	private static final String EXPIRED_TOKEN_MUNICIPALITY_ID = "2281";
	private static final String VALID_TOKEN_MUNICIPALITY_ID = "2260";
	private static final String NO_TOKEN_MUNICIPALITY_ID = "1234";

	@Mock
	private IntricTokenConfiguration intricTokenConfigurationMock;

	@InjectMocks
	private IntricTokenService intricTokenService;

	@BeforeEach
	void setup() {
		var tokenMap = new HashMap<>();
		// Token that is expired
		tokenMap.put(EXPIRED_TOKEN_MUNICIPALITY_ID, new IntricTokenService.Token("tokenValue", Instant.now()));
		// Token that is valid for another 10 seconds
		tokenMap.put(VALID_TOKEN_MUNICIPALITY_ID, new IntricTokenService.Token("tokenValue2", Instant.now().plusSeconds(10)));

		ReflectionTestUtils.setField(intricTokenService, "intricTokens", tokenMap);
	}

	@Test
	void getToken_tokenExpired() {
		var jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkFuZHJlIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

		var restClientMock = Mockito.mock(RestClient.class);
		var requestBodyUriSpecMock = Mockito.mock(RestClient.RequestBodyUriSpec.class);
		var requestBodySpecMock = Mockito.mock(RestClient.RequestBodySpec.class);
		var responseSpecMock = Mockito.mock(RestClient.ResponseSpec.class);
		var responseBodyMock = Mockito.mock(ResponseEntity.class);
		var accessToken = new AccessToken().accessToken(jwt);
		var tokenBody = new LinkedMultiValueMap<String, String>();

		when(intricTokenConfigurationMock.getTokenClient(EXPIRED_TOKEN_MUNICIPALITY_ID)).thenReturn(restClientMock);
		when(intricTokenConfigurationMock.getTokenBody(EXPIRED_TOKEN_MUNICIPALITY_ID)).thenReturn(tokenBody);
		when(restClientMock.post()).thenReturn(requestBodyUriSpecMock);
		when(requestBodyUriSpecMock.body(tokenBody)).thenReturn(requestBodySpecMock);
		when(requestBodySpecMock.retrieve()).thenReturn(responseSpecMock);
		when(responseSpecMock.toEntity(AccessToken.class)).thenReturn(responseBodyMock);
		when(responseBodyMock.getBody()).thenReturn(accessToken);

		var token = intricTokenService.getToken(EXPIRED_TOKEN_MUNICIPALITY_ID);

		assertThat(token).isEqualTo(jwt);
	}

	@Test
	void getToken_noToken() {
		var jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkFuZHJlIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

		var restClientMock = Mockito.mock(RestClient.class);
		var requestBodyUriSpecMock = Mockito.mock(RestClient.RequestBodyUriSpec.class);
		var requestBodySpecMock = Mockito.mock(RestClient.RequestBodySpec.class);
		var responseSpecMock = Mockito.mock(RestClient.ResponseSpec.class);
		var responseBodyMock = Mockito.mock(ResponseEntity.class);
		var accessToken = new AccessToken().accessToken(jwt);
		var tokenBody = new LinkedMultiValueMap<String, String>();

		when(intricTokenConfigurationMock.getTokenClient(NO_TOKEN_MUNICIPALITY_ID)).thenReturn(restClientMock);
		when(intricTokenConfigurationMock.getTokenBody(NO_TOKEN_MUNICIPALITY_ID)).thenReturn(tokenBody);
		when(restClientMock.post()).thenReturn(requestBodyUriSpecMock);
		when(requestBodyUriSpecMock.body(tokenBody)).thenReturn(requestBodySpecMock);
		when(requestBodySpecMock.retrieve()).thenReturn(responseSpecMock);
		when(responseSpecMock.toEntity(AccessToken.class)).thenReturn(responseBodyMock);
		when(responseBodyMock.getBody()).thenReturn(accessToken);

		var token = intricTokenService.getToken(NO_TOKEN_MUNICIPALITY_ID);

		assertThat(token).isEqualTo(jwt);
	}

	@Test
	void getToken_validToken() {
		var token = intricTokenService.getToken(VALID_TOKEN_MUNICIPALITY_ID);

		assertThat(token).isEqualTo("tokenValue2");
	}

	@Test
	void tokenConstructor() {
		var instant = Instant.now();
		var accessToken = "tokenValue";
		var token = new IntricTokenService.Token(accessToken, instant);

		assertThat(token.accessToken()).isEqualTo(accessToken);
		assertThat(token.expiresAt()).isEqualTo(instant);
		assertThat(token).hasNoNullFieldsOrProperties();
	}
}
