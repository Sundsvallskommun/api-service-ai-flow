package se.sundsvall.ai.flow.integration.db;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FlowEntityIdTest {

	@Test
	void constructorAndGetter() {
		var name = "tjansteskrivelser";
		var version = 1;

		var flowEntityId = new FlowEntityId(name, version);

		assertThat(flowEntityId.name()).isEqualTo(name);
		assertThat(flowEntityId.version()).isEqualTo(version);
		assertThat(flowEntityId).hasNoNullFieldsOrProperties();
	}
}
