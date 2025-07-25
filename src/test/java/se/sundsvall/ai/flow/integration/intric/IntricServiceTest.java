package se.sundsvall.ai.flow.integration.intric;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.ai.flow.integration.intric.IntricService.INPUT_DELIMITER;

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
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class IntricServiceTest {

	private static final String MUNICIPALITY_ID = "2281";

	@Mock
	private IntricIntegration intricIntegrationMock;

	@InjectMocks
	private IntricService intricService;

	@Test
	void runService() {
		var intricEndpointId = UUID.randomUUID();
		var uploadedFilesInfo = "someInfo";
		var input = "input";

		when(intricIntegrationMock.runService(eq(MUNICIPALITY_ID), eq(intricEndpointId), any(RunService.class))).thenReturn(new ServiceOutput().output("someOutput"));

		var response = intricService.runService(MUNICIPALITY_ID, intricEndpointId, List.of(), uploadedFilesInfo, input);

		assertThat(response.answer()).isEqualTo("someOutput");

		verify(intricIntegrationMock).runService(MUNICIPALITY_ID, intricEndpointId, new RunService().input(uploadedFilesInfo + INPUT_DELIMITER + input));
		verifyNoMoreInteractions(intricIntegrationMock);
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

		when(intricIntegrationMock.askAssistant(eq(MUNICIPALITY_ID), eq(intricEndpointId), any(AskAssistant.class))).thenReturn(new AskResponse().answer(answer).sessionId(intricSessionId));

		var response = intricService.askAssistant(MUNICIPALITY_ID, intricEndpointId, uploadedInputFilesInUse, uploadedInputFilesInUseInfo);

		assertThat(response.answer()).isEqualTo(answer);
		assertThat(response.sessionId()).isEqualTo(intricSessionId);

		verify(intricIntegrationMock).askAssistant(MUNICIPALITY_ID, intricEndpointId, askAssistantRequest);
		verifyNoMoreInteractions(intricIntegrationMock);
	}

	@Test
	void askAssistantFollowup() {
		var intricEndpointId = UUID.randomUUID();
		var intricSessionId = UUID.randomUUID();
		var uploadedFilesInfo = "someInfo";
		var question = "someQuestion";
		var askAssistantRequest = new AskAssistant().question(uploadedFilesInfo + INPUT_DELIMITER + question);
		var answer = "someAnswer";

		when(intricIntegrationMock.askAssistantFollowup(eq(MUNICIPALITY_ID), eq(intricEndpointId), eq(intricSessionId), any(AskAssistant.class))).thenReturn(new AskResponse().answer(answer).sessionId(intricSessionId));

		var response = intricService.askAssistantFollowup(MUNICIPALITY_ID, intricEndpointId, intricSessionId, List.of(), uploadedFilesInfo, question);

		assertThat(response.answer()).isEqualTo(answer);
		assertThat(response.sessionId()).isEqualTo(intricSessionId);

		verify(intricIntegrationMock).askAssistantFollowup(MUNICIPALITY_ID, intricEndpointId, intricSessionId, askAssistantRequest);
		verifyNoMoreInteractions(intricIntegrationMock);
	}

	@Test
	void uploadFile() {
		var file = mock(MultipartFile.class);
		var intricFileId = UUID.randomUUID();
		var filePublicResponse = new FilePublic().id(intricFileId);

		when(intricIntegrationMock.uploadFile(MUNICIPALITY_ID, file)).thenReturn(filePublicResponse);

		var uploadedFileId = intricService.uploadFile(MUNICIPALITY_ID, file);

		assertThat(uploadedFileId).isEqualTo(intricFileId);

		verify(intricIntegrationMock).uploadFile(MUNICIPALITY_ID, file);
		verifyNoMoreInteractions(intricIntegrationMock);
	}

	@Test
	void deleteFiles() {
		var fileIds = List.of(UUID.randomUUID(), UUID.randomUUID());

		doNothing().when(intricIntegrationMock).deleteFile(eq(MUNICIPALITY_ID), any(UUID.class));

		intricService.deleteFiles(MUNICIPALITY_ID, fileIds);

		verify(intricIntegrationMock).deleteFile(MUNICIPALITY_ID, fileIds.getFirst());
		verify(intricIntegrationMock).deleteFile(MUNICIPALITY_ID, fileIds.getLast());
		verifyNoMoreInteractions(intricIntegrationMock);
	}

	@Test
	void deleteFile() {
		var fileId = UUID.randomUUID();

		doNothing().when(intricIntegrationMock).deleteFile(eq(MUNICIPALITY_ID), any(UUID.class));

		intricService.deleteFile(MUNICIPALITY_ID, fileId);

		verify(intricIntegrationMock).deleteFile(MUNICIPALITY_ID, fileId);
		verifyNoMoreInteractions(intricIntegrationMock);
	}
}
