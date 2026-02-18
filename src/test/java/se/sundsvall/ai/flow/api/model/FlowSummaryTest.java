package se.sundsvall.ai.flow.api.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FlowSummaryTest {

	@Test
	void constructorAndGetter() {
		final var id = "id";
		final var version = 1;
		final var name = "name";
		final var description = "description";

		final var flowSummary = new FlowSummary(id, version, name, description);

		assertThat(flowSummary.id()).isEqualTo(id);
		assertThat(flowSummary.version()).isEqualTo(version);
		assertThat(flowSummary.name()).isEqualTo(name);
		assertThat(flowSummary.description()).isEqualTo(description);
	}
}
