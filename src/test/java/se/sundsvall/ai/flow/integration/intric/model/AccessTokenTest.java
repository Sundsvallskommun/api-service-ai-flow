package se.sundsvall.ai.flow.integration.intric.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AccessTokenTest {

	@Test
	void constructorAndGetter() {
		var accessToken = "accessToken";
		var tokenType = "tokenType";

		var token = new AccessToken(accessToken, tokenType);

		assertThat(token.accessToken()).isEqualTo(accessToken);
		assertThat(token.tokenType()).isEqualTo(tokenType);
	}

}
