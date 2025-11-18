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

import generated.eneo.ai.AppRunPublic;
import generated.eneo.ai.AskAssistant;
import generated.eneo.ai.AskResponse;
import generated.eneo.ai.FilePublic;
import generated.eneo.ai.ModelId;
import generated.eneo.ai.RunAppRequest;
import generated.eneo.ai.RunService;
import generated.eneo.ai.ServiceOutput;
import generated.eneo.ai.Status;
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
		var intricEndpointId = UUID.randomUUID();
		var uploadedFilesInfo = "someInfo";
		var input = "input";

		when(eneoIntegrationMock.runService(eq(MUNICIPALITY_ID), eq(intricEndpointId), any(RunService.class))).thenReturn(new ServiceOutput().output("someOutput"));

		var response = eneoService.runService(MUNICIPALITY_ID, intricEndpointId, List.of(), uploadedFilesInfo, input);

		assertThat(response.answer()).isEqualTo("someOutput");

		verify(eneoIntegrationMock).runService(MUNICIPALITY_ID, intricEndpointId, new RunService().input(uploadedFilesInfo + INPUT_DELIMITER + input));
		verifyNoMoreInteractions(eneoIntegrationMock);
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

		when(eneoIntegrationMock.askAssistant(eq(MUNICIPALITY_ID), eq(intricEndpointId), any(AskAssistant.class))).thenReturn(new AskResponse().answer(answer).sessionId(intricSessionId));

		var response = eneoService.askAssistant(MUNICIPALITY_ID, intricEndpointId, uploadedInputFilesInUse, uploadedInputFilesInUseInfo);

		assertThat(response.answer()).isEqualTo(answer);
		assertThat(response.sessionId()).isEqualTo(intricSessionId);

		verify(eneoIntegrationMock).askAssistant(MUNICIPALITY_ID, intricEndpointId, askAssistantRequest);
		verifyNoMoreInteractions(eneoIntegrationMock);
	}

	@Test
	void askAssistantFollowup() {
		var intricEndpointId = UUID.randomUUID();
		var intricSessionId = UUID.randomUUID();
		var uploadedFilesInfo = "someInfo";
		var question = "someQuestion";
		var askAssistantRequest = new AskAssistant().question(uploadedFilesInfo + INPUT_DELIMITER + question);
		var answer = "someAnswer";

		when(eneoIntegrationMock.askAssistantFollowup(eq(MUNICIPALITY_ID), eq(intricEndpointId), eq(intricSessionId), any(AskAssistant.class))).thenReturn(new AskResponse().answer(answer).sessionId(intricSessionId));

		var response = eneoService.askAssistantFollowup(MUNICIPALITY_ID, intricEndpointId, intricSessionId, List.of(), uploadedFilesInfo, question);

		assertThat(response.answer()).isEqualTo(answer);
		assertThat(response.sessionId()).isEqualTo(intricSessionId);

		verify(eneoIntegrationMock).askAssistantFollowup(MUNICIPALITY_ID, intricEndpointId, intricSessionId, askAssistantRequest);
		verifyNoMoreInteractions(eneoIntegrationMock);
	}

	@Test
	void uploadFile() {
		var file = mock(MultipartFile.class);
		var intricFileId = UUID.randomUUID();
		var filePublicResponse = new FilePublic().id(intricFileId);

		when(eneoIntegrationMock.uploadFile(MUNICIPALITY_ID, file)).thenReturn(filePublicResponse);

		var uploadedFileId = eneoService.uploadFile(MUNICIPALITY_ID, file);

		assertThat(uploadedFileId).isEqualTo(intricFileId);

		verify(eneoIntegrationMock).uploadFile(MUNICIPALITY_ID, file);
		verifyNoMoreInteractions(eneoIntegrationMock);
	}

	@Test
	void deleteFiles() {
		var fileIds = List.of(UUID.randomUUID(), UUID.randomUUID());

		doNothing().when(eneoIntegrationMock).deleteFile(eq(MUNICIPALITY_ID), any(UUID.class));

		eneoService.deleteFiles(MUNICIPALITY_ID, fileIds);

		verify(eneoIntegrationMock).deleteFile(MUNICIPALITY_ID, fileIds.getFirst());
		verify(eneoIntegrationMock).deleteFile(MUNICIPALITY_ID, fileIds.getLast());
		verifyNoMoreInteractions(eneoIntegrationMock);
	}

	@Test
	void deleteFile() {
		var fileId = UUID.randomUUID();

		doNothing().when(eneoIntegrationMock).deleteFile(eq(MUNICIPALITY_ID), any(UUID.class));

		eneoService.deleteFile(MUNICIPALITY_ID, fileId);

		verify(eneoIntegrationMock).deleteFile(MUNICIPALITY_ID, fileId);
		verifyNoMoreInteractions(eneoIntegrationMock);
	}

	@Test
	void runApp() {
		var appId = UUID.randomUUID();
		var uploadedInputFilesInUse = List.of(UUID.randomUUID());
		var runId = UUID.randomUUID();
		var output = "output";
		var appRunPublic = new AppRunPublic()
			.id(runId)
			.output(output)
			.status(Status.COMPLETE);

		when(eneoIntegrationMock.runApp(eq(MUNICIPALITY_ID), eq(appId), any(RunAppRequest.class))).thenReturn(appRunPublic);

		var response = eneoService.runApp(MUNICIPALITY_ID, appId, uploadedInputFilesInUse);

		assertThat(response.runId()).isEqualTo(runId);
		assertThat(response.answer()).isEqualTo(output);
		assertThat(response.status()).isEqualTo(Status.COMPLETE);

		verify(eneoIntegrationMock).runApp(eq(MUNICIPALITY_ID), eq(appId), any(RunAppRequest.class));
		verifyNoMoreInteractions(eneoIntegrationMock);
	}

	@Test
	void getAppRun() {
		var runId = UUID.randomUUID();
		var output = "App output";
		var appRunPublic = new AppRunPublic()
			.id(runId)
			.output(output)
			.status(Status.COMPLETE);

		when(eneoIntegrationMock.getAppRun(MUNICIPALITY_ID, runId)).thenReturn(appRunPublic);

		var response = eneoService.getAppRun(MUNICIPALITY_ID, runId);

		assertThat(response.runId()).isEqualTo(runId);
		assertThat(response.answer()).isEqualTo(output);
		assertThat(response.status()).isEqualTo(Status.COMPLETE);

		verify(eneoIntegrationMock).getAppRun(MUNICIPALITY_ID, runId);
		verifyNoMoreInteractions(eneoIntegrationMock);
	}
}
