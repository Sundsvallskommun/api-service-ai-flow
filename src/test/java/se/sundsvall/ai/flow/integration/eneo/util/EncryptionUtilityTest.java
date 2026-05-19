package se.sundsvall.ai.flow.integration.eneo.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EncryptionUtilityTest {

	@Mock
	private CredentialsProperties credentialsProperties;

	@InjectMocks
	private EncryptionUtility encryptionUtility;

	@Test
	void encryptAndDecrypt() {
		final var input = "someInput";

		when(credentialsProperties.secretKey()).thenReturn("WbVG8XC%m&9Z!7a$xyKGWzB^#kUSoUUs");

		final var encodedResult = encryptionUtility.encrypt(input.getBytes());
		final var decodedResult = encryptionUtility.decrypt(encodedResult);

		assertThat(decodedResult).isEqualTo(input);
	}
}
