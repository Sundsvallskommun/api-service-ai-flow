package se.sundsvall.ai.flow.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RenderRequestTest {

	@Test
	void constructorAndGetter() {
		var templateId = "templateId";

		var renderRequest = new RenderRequest(templateId);

		assertThat(renderRequest.templateId()).isEqualTo(templateId);
		assertThat(renderRequest).hasNoNullFieldsOrProperties();
		assertThat(renderRequest).hasOnlyFields("templateId");
	}
}
