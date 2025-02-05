package se.sundsvall.ai.flow.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CreateSessionRequestTest {

	@Test
	void constructorAndGetter() {
		var flowId = "someFlowId";
		var version = 123;

		var createSessionRequest = new CreateSessionRequest(flowId, version);

		assertThat(createSessionRequest.flowId()).isEqualTo(flowId);
		assertThat(createSessionRequest.version()).isEqualTo(version);
	}
}
