package se.sundsvall.ai.flow.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class FlowsTest {

	@Test
	void constructorAndGetter() {
		var flowSummaries = List.of(new FlowSummary("name", 1));

		var flows = new Flows(flowSummaries);

		assertThat(flows.flows()).isEqualTo(flowSummaries);
		assertThat(flows).hasNoNullFieldsOrProperties().hasOnlyFields("flows");
	}
}
