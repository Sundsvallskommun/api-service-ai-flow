package se.sundsvall.ai.flow.integration.db;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FlowEntityTest {

	@Test
	void constructorAndGetter() {
		var name = "tjansteskrivelser";
		var version = 1;
		var content = "content";

		var flowEntity = new FlowEntity(name, version, content);

		assertThat(flowEntity.getName()).isEqualTo(name);
		assertThat(flowEntity.getVersion()).isEqualTo(version);
		assertThat(flowEntity.getContent()).isEqualTo(content);
		assertThat(flowEntity).hasNoNullFieldsOrProperties();
	}

	@Test
	void setterAndGetter() {
		var name = "tjansteskrivelser";
		var version = 1;
		var content = "content";

		var flowEntity = new FlowEntity();
		flowEntity.setName(name);
		flowEntity.setVersion(version);
		flowEntity.setContent(content);

		assertThat(flowEntity.getName()).isEqualTo(name);
		assertThat(flowEntity.getVersion()).isEqualTo(version);
		assertThat(flowEntity.getContent()).isEqualTo(content);
		assertThat(flowEntity).hasNoNullFieldsOrProperties();
	}

	@Test
	void builderPattern() {
		var name = "tjansteskrivelser";
		var version = 1;
		var content = "content";

		var flowEntity = new FlowEntity()
			.withName(name)
			.withVersion(version)
			.withContent(content);

		assertThat(flowEntity.getName()).isEqualTo(name);
		assertThat(flowEntity.getVersion()).isEqualTo(version);
		assertThat(flowEntity.getContent()).isEqualTo(content);
		assertThat(flowEntity).hasNoNullFieldsOrProperties();
	}
}
