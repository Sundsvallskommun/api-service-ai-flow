package se.sundsvall.ai.flow.integration.templating;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.ai.flow.Application;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("junit")
class TemplatingPropertiesTest {

	@Autowired
	private TemplatingProperties properties;

	@Test
	void testProperties() {
		assertThat(properties.connectTimeoutInSeconds()).isEqualTo(5);
		assertThat(properties.readTimeoutInSeconds()).isEqualTo(15);
		assertThat(properties.baseUrl()).isEqualTo("http://base-url.com");
		assertThat(properties.oauth2()).satisfies(oauth -> {
			assertThat(oauth.tokenUrl()).isEqualTo("http://token-url.com");
			assertThat(oauth.clientId()).isEqualTo("client_id");
			assertThat(oauth.clientSecret()).isEqualTo("client_secret");
			assertThat(oauth.authorizationGrantType()).isEqualTo("client_credentials");
		});
	}
}
