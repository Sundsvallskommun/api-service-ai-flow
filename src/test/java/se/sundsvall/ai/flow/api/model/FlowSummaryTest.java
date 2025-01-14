package se.sundsvall.ai.flow.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FlowSummaryTest {

	@Test
	void constructorAndGetter() {
		var name = "name";
		var version = 1;

		var flowSummary = new FlowSummary(name, version);

		assertThat(flowSummary.name()).isEqualTo(name);
		assertThat(flowSummary.version()).isEqualTo(version);
		assertThat(flowSummary).hasNoNullFieldsOrProperties().hasOnlyFields("name", "version");
	}
}
