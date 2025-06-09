package se.sundsvall.ai.flow.integration.intric;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import generated.intric.ai.AskAssistant;
import generated.intric.ai.AskResponse;
import generated.intric.ai.FilePublic;
import generated.intric.ai.RunService;
import generated.intric.ai.ServiceOutput;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
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
class IntricIntegrationTest {

	@Mock
	private IntricClient intricClient;

	@InjectMocks
	private IntricIntegration intricIntegration;

	@AfterEach
	void afterAll() {
		verifyNoMoreInteractions(intricClient);
	}

	@Test
	void runService() {
		var serviceId = UUID.randomUUID();
		var request = new RunService();
		var serviceOutput = new ServiceOutput().output("someOutput");

		when(intricClient.runService(serviceId, request)).thenReturn(serviceOutput);

		var result = intricIntegration.runService(serviceId, request);

		assertThat(result).isNotNull().isEqualTo(serviceOutput);
		verify(intricClient).runService(serviceId, request);
	}

	@Test
	void runService_exception() {
		var serviceId = UUID.randomUUID();
		var request = new RunService();

		when(intricClient.runService(serviceId, request)).thenThrow(RuntimeException.class);

		assertThatThrownBy(() -> intricIntegration.runService(serviceId, request))
			.isInstanceOf(Problem.class)
			.hasMessageContaining("Bad Gateway")
			.hasMessageContaining("Error running service with ID: %s".formatted(serviceId));

		verify(intricClient).runService(serviceId, request);
	}

	@Test
	void askAssistant() {
		var assistantId = UUID.randomUUID();
		var request = new AskAssistant();
		var askResponse = new AskResponse()
			.sessionId(UUID.randomUUID())
			.answer("someAnswer");

		when(intricClient.askAssistant(assistantId, request)).thenReturn(askResponse);

		var result = intricIntegration.askAssistant(assistantId, request);

		assertThat(result).isNotNull().isEqualTo(askResponse);
		verify(intricClient).askAssistant(assistantId, request);
	}

	@Test
	void askAssistant_exception() {
		var assistantId = UUID.randomUUID();
		var request = new AskAssistant();

		when(intricClient.askAssistant(assistantId, request)).thenThrow(RuntimeException.class);

		assertThatThrownBy(() -> intricIntegration.askAssistant(assistantId, request))
			.isInstanceOf(Problem.class)
			.hasMessageContaining("Bad Gateway")
			.hasMessageContaining("Error asking assistant with ID: %s".formatted(assistantId));

		verify(intricClient).askAssistant(assistantId, request);
	}

	@Test
	void askAssistantFollowup() {
		var assistantId = UUID.randomUUID();
		var sessionId = UUID.randomUUID();
		var request = new AskAssistant();
		var askResponse = new AskResponse()
			.sessionId(sessionId)
			.answer("someAnswer");

		when(intricClient.askAssistantFollowup(assistantId, sessionId, request)).thenReturn(askResponse);

		var result = intricIntegration.askAssistantFollowup(assistantId, sessionId, request);

		assertThat(result).isNotNull().isEqualTo(askResponse);
		verify(intricClient).askAssistantFollowup(assistantId, sessionId, request);
	}

	@Test
	void askAssistantFollowup_exception() {
		var assistantId = UUID.randomUUID();
		var sessionId = UUID.randomUUID();
		var request = new AskAssistant();

		when(intricClient.askAssistantFollowup(assistantId, sessionId, request)).thenThrow(RuntimeException.class);

		assertThatThrownBy(() -> intricIntegration.askAssistantFollowup(assistantId, sessionId, request))
			.isInstanceOf(Problem.class)
			.hasMessageContaining("Bad Gateway")
			.hasMessageContaining("Error asking assistant with ID: %s and session ID: %s".formatted(assistantId, sessionId));

		verify(intricClient).askAssistantFollowup(assistantId, sessionId, request);
	}

	@Test
	void uploadFile() {
		var multipartFile = Mockito.mock(MultipartFile.class);
		var filePublic = new FilePublic().id(UUID.randomUUID());

		when(intricClient.uploadFile(multipartFile)).thenReturn(ResponseEntity.ok(filePublic));

		var result = intricIntegration.uploadFile(multipartFile);

		assertThat(result).isNotNull().isEqualTo(filePublic);
		verify(intricClient).uploadFile(multipartFile);
	}

	@Test
	void uploadFile_exception() {
		var multipartFile = Mockito.mock(MultipartFile.class);

		when(multipartFile.getOriginalFilename()).thenReturn("testFile.txt");
		when(intricClient.uploadFile(multipartFile)).thenThrow(RuntimeException.class);

		assertThatThrownBy(() -> intricIntegration.uploadFile(multipartFile))
			.isInstanceOf(Problem.class)
			.hasMessageContaining("Bad Gateway")
			.hasMessageContaining("Error uploading file: %s".formatted(multipartFile.getOriginalFilename()));

		verify(intricClient).uploadFile(multipartFile);
	}

	@Test
	void deleteFile() {
		var fileId = UUID.randomUUID();

		when(intricClient.deleteFile(fileId)).thenReturn(ResponseEntity.noContent().build());

		intricIntegration.deleteFile(fileId);

		verify(intricClient).deleteFile(fileId);
	}

	@Test
	void deleteFile_exception() {
		var fileId = UUID.randomUUID();

		when(intricClient.deleteFile(fileId)).thenThrow(RuntimeException.class);

		assertThatThrownBy(() -> intricIntegration.deleteFile(fileId))
			.isInstanceOf(Problem.class)
			.hasMessageContaining("Bad Gateway")
			.hasMessageContaining("Error deleting file with ID: %s".formatted(fileId));

		verify(intricClient).deleteFile(fileId);
	}
}
