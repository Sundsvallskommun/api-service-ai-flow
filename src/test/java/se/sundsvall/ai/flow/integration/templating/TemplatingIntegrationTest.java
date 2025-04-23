package se.sundsvall.ai.flow.integration.templating;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.ai.flow.TestDataFactory.createSession;

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
		var session = createSession();
		var templateId = "templateId";
		var municipalityId = "2281";
		var renderRequest = new RenderRequest()
			.identifier(templateId)
			.parameters(Map.of(
				"input1", "value",
				"step1", "BASE64:c29tZU91dHB1dF9zdGVwMQ==",
				"step2", "BASE64:c29tZU91dHB1dF9zdGVwMg==",
				"step3", "BASE64:c29tZU91dHB1dF9zdGVwMw=="));

		// Set some fake output for each step execution
		session.getStepExecutions().forEach((stepId, stepExecution) -> {
			stepExecution.setOutput("someOutput_" + stepId);
		});

		when(templatingClientMock.render(municipalityId, renderRequest)).thenReturn(new RenderResponse().output("output"));

		var result = templatingIntegration.renderSession(session, templateId, municipalityId);

		assertThat(result).isNotNull().isEqualTo("output");
		verify(templatingClientMock).render(municipalityId, renderRequest);
		verifyNoMoreInteractions(templatingClientMock);
	}
}
