package se.sundsvall.ai.flow.integration.templating;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.ai.flow.TestDataFactory.createNewSession;

import generated.se.sundsvall.templating.RenderRequest;
import generated.se.sundsvall.templating.RenderResponse;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TemplatingIntegrationTest {

	@Mock
	private TemplatingClient templatingClientMock;

	@InjectMocks
	private TemplatingIntegration templatingIntegration;

	@Test
	void renderSession() {
		var session = createNewSession();
		var templateId = "templateId";
		var municipalityId = "2281";
		// "Ärendenummer" and "value" are taken from the session created in this test and are the expected values.
		var renderRequest = new RenderRequest().identifier(templateId).parameters(Map.of("Ärendenummer", "value"));

		when(templatingClientMock.render(municipalityId, renderRequest)).thenReturn(new RenderResponse().output("output"));

		var result = templatingIntegration.renderSession(session, templateId, municipalityId);

		assertThat(result).isNotNull().isEqualTo("output");
		verify(templatingClientMock).render(municipalityId, renderRequest);
		verifyNoMoreInteractions(templatingClientMock);
	}

}
