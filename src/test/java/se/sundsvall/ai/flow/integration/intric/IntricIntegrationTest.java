package se.sundsvall.ai.flow.integration.intric;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.ai.flow.integration.intric.IntricIntegration.INPUT_DELIMITER;

import generated.intric.ai.AskAssistant;
import generated.intric.ai.AskResponse;
import generated.intric.ai.FilePublic;
import generated.intric.ai.ModelId;
import generated.intric.ai.RunService;
import generated.intric.ai.ServiceOutput;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class IntricIntegrationTest {

	@Mock
	private IntricClient intricClientMock;

	@InjectMocks
	private IntricIntegration intricIntegration;

	@Test
	void runService() {
		var intricEndpointId = UUID.randomUUID();
		var uploadedFilesInfo = "someInfo";
		var input = "input";

		when(intricClientMock.runService(eq(intricEndpointId), any(RunService.class))).thenReturn(new ServiceOutput().output("someOutput"));

		var response = intricIntegration.runService(intricEndpointId, List.of(), uploadedFilesInfo, input);

		assertThat(response.answer()).isEqualTo("someOutput");

		verify(intricClientMock).runService(intricEndpointId, new RunService().input(uploadedFilesInfo + INPUT_DELIMITER + input));
		verifyNoMoreInteractions(intricClientMock);
	}

	@Test
	void askAssistant() {
		var intricEndpointId = UUID.randomUUID();
		var uploadedInputFilesInUse = List.of(UUID.randomUUID(), UUID.randomUUID());
		var uploadedInputFilesInUseInfo = "someInfo";
		var askAssistantRequest = new AskAssistant()
			.question(uploadedInputFilesInUseInfo)
			.files(uploadedInputFilesInUse.stream().map(id -> new ModelId().id(id)).toList());
		var intricSessionId = UUID.randomUUID();
		var answer = "someAnswer";

		when(intricClientMock.askAssistant(eq(intricEndpointId), any(AskAssistant.class))).thenReturn(new AskResponse().answer(answer).sessionId(intricSessionId));

		var response = intricIntegration.askAssistant(intricEndpointId, uploadedInputFilesInUse, uploadedInputFilesInUseInfo);

		assertThat(response.answer()).isEqualTo(answer);
		assertThat(response.sessionId()).isEqualTo(intricSessionId);

		verify(intricClientMock).askAssistant(intricEndpointId, askAssistantRequest);
		verifyNoMoreInteractions(intricClientMock);
	}

	@Test
	void askAssistantFollowup() {
		var intricEndpointId = UUID.randomUUID();
		var intricSessionId = UUID.randomUUID();
		var uploadedFilesInfo = "someInfo";
		var question = "someQuestion";
		var askAssistantRequest = new AskAssistant().question(uploadedFilesInfo + INPUT_DELIMITER + question);
		var answer = "someAnswer";

		when(intricClientMock.askAssistantFollowup(eq(intricEndpointId), eq(intricSessionId), any(AskAssistant.class))).thenReturn(new AskResponse().answer(answer).sessionId(intricSessionId));

		var response = intricIntegration.askAssistantFollowup(intricEndpointId, intricSessionId, List.of(), uploadedFilesInfo, question);

		assertThat(response.answer()).isEqualTo(answer);
		assertThat(response.sessionId()).isEqualTo(intricSessionId);

		verify(intricClientMock).askAssistantFollowup(intricEndpointId, intricSessionId, askAssistantRequest);
		verifyNoMoreInteractions(intricClientMock);
	}

	@Test
	void uploadFile() {
		var file = mock(MultipartFile.class);
		var intricFileId = UUID.randomUUID();
		var filePublicResponse = new FilePublic().id(intricFileId);

		when(intricClientMock.uploadFile(file)).thenReturn(ResponseEntity.ok(filePublicResponse));

		var uploadedFileId = intricIntegration.uploadFile(file);

		assertThat(uploadedFileId).isEqualTo(intricFileId);

		verify(intricClientMock).uploadFile(file);
		verifyNoMoreInteractions(intricClientMock);
	}

	@Test
	void deleteFiles() {
		var fileIds = List.of(UUID.randomUUID(), UUID.randomUUID());

		when(intricClientMock.deleteFile(any(UUID.class))).thenReturn(ResponseEntity.noContent().build());

		intricIntegration.deleteFiles(fileIds);

		verify(intricClientMock).deleteFile(fileIds.getFirst());
		verify(intricClientMock).deleteFile(fileIds.getLast());
		verifyNoMoreInteractions(intricClientMock);
	}

	@Test
	void deleteFile() {
		var fileId = UUID.randomUUID();

		when(intricClientMock.deleteFile(fileId)).thenReturn(ResponseEntity.noContent().build());

		intricIntegration.deleteFile(fileId);

		verify(intricClientMock).deleteFile(fileId);
		verifyNoMoreInteractions(intricClientMock);
	}
}
