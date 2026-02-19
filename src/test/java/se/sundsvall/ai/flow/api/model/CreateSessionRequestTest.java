package se.sundsvall.ai.flow.api.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
