package se.sundsvall.ai.flow.integration.intric;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class IntricMapperTest {

	@Test
	void mapToRunServiceTest() {
		var input = "Test input";
		var uploadedInputFile = UUID.randomUUID();
		var uploadedInputFilesInUse = List.of(uploadedInputFile);

		var runService = IntricMapper.mapToRunService(input, uploadedInputFilesInUse);

		assertThat(runService).isNotNull().satisfies(service -> {
			assertThat(service.getInput()).isEqualTo(input);
			assertThat(service.getFiles()).hasSize(1);
			assertThat(service.getFiles().getFirst().getId()).isEqualTo(uploadedInputFile);
		});
	}

	@Test
	void toModelIdsTest() {
		var id1 = UUID.randomUUID();
		var id2 = UUID.randomUUID();
		var ids = List.of(id1, id2);

		var modelIds = IntricMapper.toModelIds(ids);

		assertThat(modelIds).isNotNull().hasSize(2)
			.extracting("id")
			.containsExactlyInAnyOrder(id1, id2);
	}

	@Test
	void mapToAskAssistantTest() {
		var question = "What is the meaning of life?";
		var uploadedInputFile = UUID.randomUUID();
		var uploadedInputFilesInUse = List.of(uploadedInputFile);

		var askAssistant = IntricMapper.mapToAskAssistant(question, uploadedInputFilesInUse);

		assertThat(askAssistant).isNotNull().satisfies(assistant -> {
			assertThat(assistant.getQuestion()).isEqualTo(question);
			assertThat(assistant.getFiles()).hasSize(1);
			assertThat(assistant.getFiles().getFirst().getId()).isEqualTo(uploadedInputFile);
		});
	}

	@Test
	void mapToResponseFromAskResponseTest() {
		var sessionId = UUID.randomUUID();
		var answer = "42";
		var askResponse = new generated.intric.ai.AskResponse().sessionId(sessionId).answer(answer);

		var response = IntricMapper.mapToResponse(askResponse);

		assertThat(response).isNotNull().satisfies(res -> {
			assertThat(res.sessionId()).isEqualTo(sessionId);
			assertThat(res.answer()).isEqualTo(answer);
		});
	}

	@Test
	void mapToResponseFromServiceOutputTest() {
		var output = "Service output";
		var serviceOutput = new generated.intric.ai.ServiceOutput().output(output);

		var response = IntricMapper.mapToResponse(serviceOutput);

		assertThat(response).isNotNull().satisfies(res -> {
			assertThat(res.sessionId()).isNull();
			assertThat(res.answer()).isEqualTo(output);
		});
	}

}
