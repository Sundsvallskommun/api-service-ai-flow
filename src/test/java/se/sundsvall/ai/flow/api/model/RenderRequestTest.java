package se.sundsvall.ai.flow.api.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RenderRequestTest {

	@Test
	void constructorAndGetter() {
		var templateId = "templateId";

		var renderRequest = new RenderRequest(templateId);

		assertThat(renderRequest.templateId()).isEqualTo(templateId);
		assertThat(renderRequest).hasNoNullFieldsOrProperties().hasOnlyFields("templateId");
	}
}
