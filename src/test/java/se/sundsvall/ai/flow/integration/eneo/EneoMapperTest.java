package se.sundsvall.ai.flow.integration.eneo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import generated.eneo.ai.AskResponse;
import generated.eneo.ai.ServiceOutput;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

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
		var askResponse = new AskResponse().sessionId(sessionId).answer(answer);

		var response = EneoMapper.toResponse(askResponse);

		assertThat(response).isNotNull().satisfies(res -> {
			assertThat(res.sessionId()).isEqualTo(sessionId);
			assertThat(res.answer()).isEqualTo(answer);
		});
	}

	@Test
	void toResponseFromServiceOutputTest() {
		final var output = "Service output";
		final var serviceOutput = new ServiceOutput().output(output);

		final var response = EneoMapper.toResponse(serviceOutput);

		assertThat(response).isNotNull().satisfies(res -> {
			assertThat(res.sessionId()).isNull();
			assertThat(res.answer()).isEqualTo(output);
		});
	}

	@Test
	void toResponseFromServiceOutputWithMapTest() {
		final var output = new LinkedHashMap<String, Object>();
		output.put("key", "value");
		output.put("number", 42);
		final var serviceOutput = new ServiceOutput().output(output);

		final var response = EneoMapper.toResponse(serviceOutput);

		assertThat(response).isNotNull().satisfies(res -> {
			assertThat(res.sessionId()).isNull();
			assertThat(res.answer()).isEqualTo("{\"key\":\"value\",\"number\":42}");
		});
	}

	@Test
	void toResponseFromServiceOutputWithListTest() {
		final var output = List.of("item1", "item2", "item3");
		final var serviceOutput = new ServiceOutput().output(output);

		final var response = EneoMapper.toResponse(serviceOutput);

		assertThat(response).isNotNull().satisfies(res -> {
			assertThat(res.sessionId()).isNull();
			assertThat(res.answer()).isEqualTo("[\"item1\",\"item2\",\"item3\"]");
		});
	}

	@Test
	void toResponseFromServiceOutputWithBooleanTest() {
		final var serviceOutput = new ServiceOutput().output(true);

		final var response = EneoMapper.toResponse(serviceOutput);

		assertThat(response).isNotNull().satisfies(res -> {
			assertThat(res.sessionId()).isNull();
			assertThat(res.answer()).isEqualTo("true");
		});
	}

	@Test
	void toResponseFromServiceOutputWithNullTest() {
		final var serviceOutput = new ServiceOutput().output(null);

		final var response = EneoMapper.toResponse(serviceOutput);

		assertThat(response).isNotNull().satisfies(res -> {
			assertThat(res.sessionId()).isNull();
			assertThat(res.answer()).isNull();
		});
	}

	@Test
	void toResponseFromServiceOutputWithUnserializableObjectThrowsException() {
		// Create a circular reference to force Jackson serialization failure
		final var circularMap = new LinkedHashMap<String, Object>();
		circularMap.put("self", Collections.unmodifiableMap(circularMap));
		final var serviceOutput = new ServiceOutput().output(circularMap);

		assertThatExceptionOfType(RuntimeException.class)
			.isThrownBy(() -> EneoMapper.toResponse(serviceOutput))
			.withMessage("Internal Server Error: Failed to serialize ServiceOutput to JSON string");
	}

}
