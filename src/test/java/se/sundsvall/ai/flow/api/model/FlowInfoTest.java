package se.sundsvall.ai.flow.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FlowInfoTest {

	@Test
	void constructorAndGetter() {
		var id = "id";
		var name = "name";
		var description = "description";
		var defaultTemplateId = "defaultTemplateId";

		var flowinfo = new FlowInfo(id, name, description, defaultTemplateId);

		assertThat(flowinfo.id()).isEqualTo(id);
		assertThat(flowinfo.name()).isEqualTo(name);
		assertThat(flowinfo.description()).isEqualTo(description);
		assertThat(flowinfo.defaultTemplateId()).isEqualTo(defaultTemplateId);
		assertThat(flowinfo).hasNoNullFieldsOrProperties().hasOnlyFields("id", "name", "description", "defaultTemplateId");
	}

}
