package se.sundsvall.ai.flow.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FlowResponseTest {

	@Test
	void constructorAndGetter() {
		var name = "name";
		var version = 1;
		var content = "content";

		var flowResponse = new FlowResponse(name, version, content);

		assertThat(flowResponse.name()).isEqualTo(name);
		assertThat(flowResponse.version()).isEqualTo(version);
		assertThat(flowResponse.content()).isEqualTo(content);
		assertThat(flowResponse).hasNoNullFieldsOrProperties().hasOnlyFields("name", "version", "content");
	}
}
