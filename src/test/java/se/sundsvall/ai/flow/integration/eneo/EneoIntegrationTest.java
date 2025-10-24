package se.sundsvall.ai.flow.integration.eneo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.ai.flow.TestDataFactory.MUNICIPALITY_ID;

import generated.eneo.ai.AskAssistant;
import generated.eneo.ai.AskResponse;
import generated.eneo.ai.FilePublic;
import generated.eneo.ai.RunService;
import generated.eneo.ai.ServiceOutput;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.zalando.problem.Problem;

@ExtendWith(MockitoExtension.class)
class EneoIntegrationTest {

	@Mock
	private EneoClient mockEneoClient;

	@Mock
	private Map<String, EneoClient> mockEneoClients;

	@InjectMocks
	private EneoIntegration eneoIntegration;

	@BeforeEach
	void setup() {
		when(mockEneoClients.get(MUNICIPALITY_ID)).thenReturn(mockEneoClient);
	}

	@AfterEach
	void afterAll() {
		verifyNoMoreInteractions(mockEneoClients, mockEneoClient);
	}

	@Test
	void runService() {
		final var serviceId = UUID.randomUUID();
		final var request = new RunService();
		final var serviceOutput = new ServiceOutput().output("someOutput");

		when(mockEneoClient.runService(serviceId, request)).thenReturn(serviceOutput);

		final var result = eneoIntegration.runService(MUNICIPALITY_ID, serviceId, request);

		assertThat(result).isNotNull().isEqualTo(serviceOutput);
		verify(mockEneoClient).runService(serviceId, request);
	}

	@Test
	void runService_exception() {
		final var serviceId = UUID.randomUUID();
		final var request = new RunService();

		when(mockEneoClient.runService(serviceId, request)).thenThrow(RuntimeException.class);

		assertThatThrownBy(() -> eneoIntegration.runService(MUNICIPALITY_ID, serviceId, request))
			.isInstanceOf(Problem.class)
			.hasMessageContaining("Bad Gateway")
			.hasMessageContaining("Error running service with ID: %s".formatted(serviceId));

		verify(mockEneoClient).runService(serviceId, request);
	}

	@Test
	void askAssistant() {
		final var assistantId = UUID.randomUUID();
		final var request = new AskAssistant();
		final var askResponse = new AskResponse()
			.sessionId(UUID.randomUUID())
			.answer("someAnswer");

		when(mockEneoClient.askAssistant(assistantId, request)).thenReturn(askResponse);

		final var result = eneoIntegration.askAssistant(MUNICIPALITY_ID, assistantId, request);

		assertThat(result).isNotNull().isEqualTo(askResponse);
		verify(mockEneoClient).askAssistant(assistantId, request);
	}

	@Test
	void askAssistant_exception() {
		final var assistantId = UUID.randomUUID();
		final var request = new AskAssistant();

		when(mockEneoClient.askAssistant(assistantId, request)).thenThrow(RuntimeException.class);

		assertThatThrownBy(() -> eneoIntegration.askAssistant(MUNICIPALITY_ID, assistantId, request))
			.isInstanceOf(Problem.class)
			.hasMessageContaining("Bad Gateway")
			.hasMessageContaining("Error asking assistant with ID: %s".formatted(assistantId));

		verify(mockEneoClient).askAssistant(assistantId, request);
	}

	@Test
	void askAssistantFollowup() {
		final var assistantId = UUID.randomUUID();
		final var sessionId = UUID.randomUUID();
		final var request = new AskAssistant();
		final var askResponse = new AskResponse()
			.sessionId(sessionId)
			.answer("someAnswer");

		when(mockEneoClient.askAssistantFollowup(assistantId, sessionId, request)).thenReturn(askResponse);

		final var result = eneoIntegration.askAssistantFollowup(MUNICIPALITY_ID, assistantId, sessionId, request);

		assertThat(result).isNotNull().isEqualTo(askResponse);
		verify(mockEneoClient).askAssistantFollowup(assistantId, sessionId, request);
	}

	@Test
	void askAssistantFollowup_exception() {
		final var assistantId = UUID.randomUUID();
		final var sessionId = UUID.randomUUID();
		final var request = new AskAssistant();

		when(mockEneoClient.askAssistantFollowup(assistantId, sessionId, request)).thenThrow(RuntimeException.class);

		assertThatThrownBy(() -> eneoIntegration.askAssistantFollowup(MUNICIPALITY_ID, assistantId, sessionId, request))
			.isInstanceOf(Problem.class)
			.hasMessageContaining("Bad Gateway")
			.hasMessageContaining("Error asking assistant with ID: %s and session ID: %s".formatted(assistantId, sessionId));

		verify(mockEneoClient).askAssistantFollowup(assistantId, sessionId, request);
	}

	@Test
	void uploadFile() {
		final var multipartFile = Mockito.mock(MultipartFile.class);
		final var filePublic = new FilePublic().id(UUID.randomUUID());

		when(mockEneoClient.uploadFile(multipartFile)).thenReturn(ResponseEntity.ok(filePublic));

		final var result = eneoIntegration.uploadFile(MUNICIPALITY_ID, multipartFile);

		assertThat(result).isNotNull().isEqualTo(filePublic);
		verify(mockEneoClient).uploadFile(multipartFile);
	}

	@Test
	void uploadFile_exception() {
		final var multipartFile = Mockito.mock(MultipartFile.class);

		when(multipartFile.getOriginalFilename()).thenReturn("testFile.txt");
		when(mockEneoClient.uploadFile(multipartFile)).thenThrow(RuntimeException.class);

		assertThatThrownBy(() -> eneoIntegration.uploadFile(MUNICIPALITY_ID, multipartFile))
			.isInstanceOf(Problem.class)
			.hasMessageContaining("Bad Gateway")
			.hasMessageContaining("Error uploading file: %s".formatted(multipartFile.getOriginalFilename()));

		verify(mockEneoClient).uploadFile(multipartFile);
	}

	@Test
	void deleteFile() {
		final var fileId = UUID.randomUUID();

		when(mockEneoClient.deleteFile(fileId)).thenReturn(ResponseEntity.noContent().build());

		eneoIntegration.deleteFile(MUNICIPALITY_ID, fileId);

		verify(mockEneoClient).deleteFile(fileId);
	}

	@Test
	void deleteFile_exception() {
		final var fileId = UUID.randomUUID();

		when(mockEneoClient.deleteFile(fileId)).thenThrow(RuntimeException.class);

		assertThatThrownBy(() -> eneoIntegration.deleteFile(MUNICIPALITY_ID, fileId))
			.isInstanceOf(Problem.class)
			.hasMessageContaining("Bad Gateway")
			.hasMessageContaining("Error deleting file with ID: %s".formatted(fileId));

		verify(mockEneoClient).deleteFile(fileId);
	}
}
