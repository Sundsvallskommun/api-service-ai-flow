package se.sundsvall.ai.flow.integration.eneo;

import generated.eneo.ai.AppRunPublic;
import generated.eneo.ai.ServiceOutput;
import generated.eneo.ai.Status;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EneoMapperTest {

	@Test
	void toRunServiceTest() {
		var input = "Test input";
		var uploadedInputFile = UUID.randomUUID();
		var uploadedInputFilesInUse = List.of(uploadedInputFile);

		var runService = EneoMapper.toRunService(input, uploadedInputFilesInUse);

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

		var modelIds = EneoMapper.toModelIds(ids);

		assertThat(modelIds).isNotNull().hasSize(2)
			.extracting("id")
			.containsExactlyInAnyOrder(id1, id2);
	}

	@Test
	void toAskAssistantTest() {
		var question = "What is the meaning of life?";
		var uploadedInputFile = UUID.randomUUID();
		var uploadedInputFilesInUse = List.of(uploadedInputFile);

		var askAssistant = EneoMapper.toAskAssistant(question, uploadedInputFilesInUse);

		assertThat(askAssistant).isNotNull().satisfies(assistant -> {
			assertThat(assistant.getQuestion()).isEqualTo(question);
			assertThat(assistant.getFiles()).hasSize(1);
			assertThat(assistant.getFiles().getFirst().getId()).isEqualTo(uploadedInputFile);
		});
	}

	@Test
	void toResponseFromAskResponseTest() {
		var sessionId = UUID.randomUUID();
		var answer = "42";
		var askResponse = new generated.eneo.ai.AskResponse().sessionId(sessionId).answer(answer);

		var response = EneoMapper.toResponse(askResponse);

		assertThat(response).isNotNull().satisfies(res -> {
			assertThat(res.sessionId()).isEqualTo(sessionId);
			assertThat(res.answer()).isEqualTo(answer);
		});
	}

	@Test
	void toResponseFromServiceOutputTest() {
		var output = "Service output";
		var serviceOutput = new ServiceOutput().output(output);

		var response = EneoMapper.toResponse(serviceOutput);

		assertThat(response).isNotNull().satisfies(res -> {
			assertThat(res.sessionId()).isNull();
			assertThat(res.answer()).isEqualTo(output);
		});
	}

	@Test
	void toRunAppRequestRequestTest() {
		var inputFileUuid = UUID.randomUUID();
		var uploadedInputFilesInUse = List.of(inputFileUuid);

		var runAppRequest = EneoMapper.toRunAppRequest(uploadedInputFilesInUse);

		assertThat(runAppRequest).isNotNull().satisfies(request -> {
			assertThat(request.getFiles()).hasSize(1);
			assertThat(request.getFiles().getFirst().getId()).isEqualTo(inputFileUuid);
		});
	}

	@Test
	void toResponseFromAppRunPublicTest() {
		var runId = UUID.randomUUID();
		var output = "output";
		var status = Status.COMPLETE;
		var appRunPublic = new AppRunPublic()
			.id(runId)
			.output(output)
			.status(status);

		var response = EneoMapper.toResponse(appRunPublic);

		assertThat(response).isNotNull().satisfies(res -> {
			assertThat(res.runId()).isEqualTo(runId);
			assertThat(res.answer()).isEqualTo(output);
			assertThat(res.status()).isEqualTo(status);
			assertThat(res.sessionId()).isNull();
		});
	}

}
