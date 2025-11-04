package se.sundsvall.ai.flow.integration.eneo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.ai.flow.integration.eneo.EneoService.INPUT_DELIMITER;

import generated.eneo.ai.AskAssistant;
import generated.eneo.ai.AskResponse;
import generated.eneo.ai.FilePublic;
import generated.eneo.ai.ModelId;
import generated.eneo.ai.RunService;
import generated.eneo.ai.ServiceOutput;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class EneoServiceTest {

	private static final String MUNICIPALITY_ID = "2281";

	@Mock
	private EneoIntegration eneoIntegrationMock;

	@InjectMocks
	private EneoService eneoService;

	@Test
	void runService() {
		final var targetId = UUID.randomUUID();
		final var uploadedFilesInfo = "someInfo";
		final var input = "input";

		when(eneoIntegrationMock.runService(eq(MUNICIPALITY_ID), eq(targetId), any(RunService.class))).thenReturn(new ServiceOutput().output("someOutput"));

		final var response = eneoService.runService(MUNICIPALITY_ID, targetId, List.of(), uploadedFilesInfo, input);

		assertThat(response.answer()).isEqualTo("someOutput");

		verify(eneoIntegrationMock).runService(MUNICIPALITY_ID, targetId, new RunService().input(uploadedFilesInfo + INPUT_DELIMITER + input));
		verifyNoMoreInteractions(eneoIntegrationMock);
	}

	@Test
	void askAssistant() {
		final var targetId = UUID.randomUUID();
		final var uploadedInputFilesInUse = List.of(UUID.randomUUID(), UUID.randomUUID());
		final var uploadedInputFilesInUseInfo = "someInfo";
		final var askAssistantRequest = new AskAssistant()
			.question(uploadedInputFilesInUseInfo)
			.files(uploadedInputFilesInUse.stream().map(id -> new ModelId().id(id)).toList());
		final var intricSessionId = UUID.randomUUID();
		final var answer = "someAnswer";

		when(eneoIntegrationMock.askAssistant(eq(MUNICIPALITY_ID), eq(targetId), any(AskAssistant.class))).thenReturn(new AskResponse().answer(answer).sessionId(intricSessionId));

		final var response = eneoService.askAssistant(MUNICIPALITY_ID, targetId, uploadedInputFilesInUse, uploadedInputFilesInUseInfo);

		assertThat(response.answer()).isEqualTo(answer);
		assertThat(response.sessionId()).isEqualTo(intricSessionId);

		verify(eneoIntegrationMock).askAssistant(MUNICIPALITY_ID, targetId, askAssistantRequest);
		verifyNoMoreInteractions(eneoIntegrationMock);
	}

	@Test
	void askAssistantFollowup() {
		final var targetId = UUID.randomUUID();
		final var intricSessionId = UUID.randomUUID();
		final var uploadedFilesInfo = "someInfo";
		final var question = "someQuestion";
		final var askAssistantRequest = new AskAssistant().question(uploadedFilesInfo + INPUT_DELIMITER + question);
		final var answer = "someAnswer";

		when(eneoIntegrationMock.askAssistantFollowup(eq(MUNICIPALITY_ID), eq(targetId), eq(intricSessionId), any(AskAssistant.class))).thenReturn(new AskResponse().answer(answer).sessionId(intricSessionId));

		final var response = eneoService.askAssistantFollowup(MUNICIPALITY_ID, targetId, intricSessionId, List.of(), uploadedFilesInfo, question);

		assertThat(response.answer()).isEqualTo(answer);
		assertThat(response.sessionId()).isEqualTo(intricSessionId);

		verify(eneoIntegrationMock).askAssistantFollowup(MUNICIPALITY_ID, targetId, intricSessionId, askAssistantRequest);
		verifyNoMoreInteractions(eneoIntegrationMock);
	}

	@Test
	void uploadFile() {
		final var file = mock(MultipartFile.class);
		final var intricFileId = UUID.randomUUID();
		final var filePublicResponse = new FilePublic().id(intricFileId);

		when(eneoIntegrationMock.uploadFile(MUNICIPALITY_ID, file)).thenReturn(filePublicResponse);

		final var uploadedFileId = eneoService.uploadFile(MUNICIPALITY_ID, file);

		assertThat(uploadedFileId).isEqualTo(intricFileId);

		verify(eneoIntegrationMock).uploadFile(MUNICIPALITY_ID, file);
		verifyNoMoreInteractions(eneoIntegrationMock);
	}

	@Test
	void deleteFiles() {
		final var fileIds = List.of(UUID.randomUUID(), UUID.randomUUID());

		doNothing().when(eneoIntegrationMock).deleteFile(eq(MUNICIPALITY_ID), any(UUID.class));

		eneoService.deleteFiles(MUNICIPALITY_ID, fileIds);

		verify(eneoIntegrationMock).deleteFile(MUNICIPALITY_ID, fileIds.getFirst());
		verify(eneoIntegrationMock).deleteFile(MUNICIPALITY_ID, fileIds.getLast());
		verifyNoMoreInteractions(eneoIntegrationMock);
	}

	@Test
	void deleteFile() {
		final var fileId = UUID.randomUUID();

		doNothing().when(eneoIntegrationMock).deleteFile(eq(MUNICIPALITY_ID), any(UUID.class));

		eneoService.deleteFile(MUNICIPALITY_ID, fileId);

		verify(eneoIntegrationMock).deleteFile(MUNICIPALITY_ID, fileId);
		verifyNoMoreInteractions(eneoIntegrationMock);
	}
}
