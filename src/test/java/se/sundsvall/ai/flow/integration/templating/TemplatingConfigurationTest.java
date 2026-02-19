package se.sundsvall.ai.flow.integration.templating;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;
import se.sundsvall.dept44.configuration.feign.decoder.ProblemErrorDecoder;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.ai.flow.integration.templating.TemplatingIntegration.CLIENT_ID;

@ExtendWith(MockitoExtension.class)
class TemplatingConfigurationTest {

	@Spy
	private FeignMultiCustomizer feignMultiCustomizerSpy;

	@Mock
	private FeignBuilderCustomizer feignBuilderCustomizerMock;

	@Mock
	private TemplatingProperties propertiesMock;

	@Test
	void testFeignBuilderCustomizer() {
		final var configuration = new TemplatingConfiguration();

		when(propertiesMock.connectTimeoutInSeconds()).thenReturn(5);
		when(propertiesMock.readTimeoutInSeconds()).thenReturn(15);
		when(propertiesMock.oauth2()).thenReturn(new TemplatingProperties.Oauth2("tokenUrl", "clientId", "clientSecret", "grantType"));
		when(feignMultiCustomizerSpy.composeCustomizersToOne()).thenReturn(feignBuilderCustomizerMock);

		try (final MockedStatic<FeignMultiCustomizer> feignMultiCustomizerMock = Mockito.mockStatic(FeignMultiCustomizer.class)) {
			feignMultiCustomizerMock.when(FeignMultiCustomizer::create).thenReturn(feignMultiCustomizerSpy);

			final var customizer = configuration.feignBuilderCustomizer(propertiesMock);

			final ArgumentCaptor<ProblemErrorDecoder> errorDecoderCaptor = ArgumentCaptor.forClass(ProblemErrorDecoder.class);

			verify(feignMultiCustomizerSpy).withErrorDecoder(errorDecoderCaptor.capture());
			verify(propertiesMock).connectTimeoutInSeconds();
			verify(propertiesMock).readTimeoutInSeconds();
			verify(feignMultiCustomizerSpy).withRequestTimeoutsInSeconds(5, 15);
			verify(feignMultiCustomizerSpy).composeCustomizersToOne();

			assertThat(errorDecoderCaptor.getValue()).hasFieldOrPropertyWithValue("integrationName", CLIENT_ID);
			assertThat(customizer).isSameAs(feignBuilderCustomizerMock);
		}
	}
}
