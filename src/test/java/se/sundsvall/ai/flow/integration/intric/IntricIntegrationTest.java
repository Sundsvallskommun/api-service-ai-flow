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
import java.util.HashMap;
import java.util.List;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.zalando.problem.Problem;
import se.sundsvall.ai.flow.integration.db.InstanceRepository;
import se.sundsvall.ai.flow.integration.db.model.InstanceEntity;
import se.sundsvall.ai.flow.integration.intric.configuration.IntricClientFactory;

@ExtendWith(MockitoExtension.class)
class IntricIntegrationTest {

	private static final String MUNICIPALITY_ID = "2281";

	@Mock
	private IntricClient intricClient;

	@Mock
	private IntricClientFactory intricClientFactory;

	@Mock
	private InstanceRepository instanceRepository;

	@InjectMocks
	private IntricIntegration intricIntegration;

	@BeforeEach
	void setup() {
		var intricClients = new HashMap<String, IntricClient>();
		intricClients.put(MUNICIPALITY_ID, intricClient);
		ReflectionTestUtils.setField(intricIntegration, "intricClients", intricClients);
	}

	@AfterEach
	void afterAll() {
		verifyNoMoreInteractions(intricClient);
	}

	@Test
	void init() {
		var instanceEntity = new InstanceEntity()
			.withMunicipalityId(MUNICIPALITY_ID);
		when(instanceRepository.findAll()).thenReturn(List.of(instanceEntity));

		intricIntegration.init();

		verify(intricClientFactory).createIntricClient(instanceEntity);
		assertThat(intricIntegration.intricClients).isNotEmpty();
	}

	@Test
	void runService() {
		var serviceId = UUID.randomUUID();
		var request = new RunService();
		var serviceOutput = new ServiceOutput().output("someOutput");

		when(intricClient.runService(serviceId, request)).thenReturn(serviceOutput);

		var result = intricIntegration.runService(MUNICIPALITY_ID, serviceId, request);

		assertThat(result).isNotNull().isEqualTo(serviceOutput);
		verify(intricClient).runService(serviceId, request);
	}

	@Test
	void runService_exception() {
		var serviceId = UUID.randomUUID();
		var request = new RunService();

		when(intricClient.runService(serviceId, request)).thenThrow(RuntimeException.class);

		assertThatThrownBy(() -> intricIntegration.runService(MUNICIPALITY_ID, serviceId, request))
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

		var result = intricIntegration.askAssistant(MUNICIPALITY_ID, assistantId, request);

		assertThat(result).isNotNull().isEqualTo(askResponse);
		verify(intricClient).askAssistant(assistantId, request);
	}

	@Test
	void askAssistant_exception() {
		var assistantId = UUID.randomUUID();
		var request = new AskAssistant();

		when(intricClient.askAssistant(assistantId, request)).thenThrow(RuntimeException.class);

		assertThatThrownBy(() -> intricIntegration.askAssistant(MUNICIPALITY_ID, assistantId, request))
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

		var result = intricIntegration.askAssistantFollowup(MUNICIPALITY_ID, assistantId, sessionId, request);

		assertThat(result).isNotNull().isEqualTo(askResponse);
		verify(intricClient).askAssistantFollowup(assistantId, sessionId, request);
	}

	@Test
	void askAssistantFollowup_exception() {
		var assistantId = UUID.randomUUID();
		var sessionId = UUID.randomUUID();
		var request = new AskAssistant();

		when(intricClient.askAssistantFollowup(assistantId, sessionId, request)).thenThrow(RuntimeException.class);

		assertThatThrownBy(() -> intricIntegration.askAssistantFollowup(MUNICIPALITY_ID, assistantId, sessionId, request))
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

		var result = intricIntegration.uploadFile(MUNICIPALITY_ID, multipartFile);

		assertThat(result).isNotNull().isEqualTo(filePublic);
		verify(intricClient).uploadFile(multipartFile);
	}

	@Test
	void uploadFile_exception() {
		var multipartFile = Mockito.mock(MultipartFile.class);

		when(multipartFile.getOriginalFilename()).thenReturn("testFile.txt");
		when(intricClient.uploadFile(multipartFile)).thenThrow(RuntimeException.class);

		assertThatThrownBy(() -> intricIntegration.uploadFile(MUNICIPALITY_ID, multipartFile))
			.isInstanceOf(Problem.class)
			.hasMessageContaining("Bad Gateway")
			.hasMessageContaining("Error uploading file: %s".formatted(multipartFile.getOriginalFilename()));

		verify(intricClient).uploadFile(multipartFile);
	}

	@Test
	void deleteFile() {
		var fileId = UUID.randomUUID();

		when(intricClient.deleteFile(fileId)).thenReturn(ResponseEntity.noContent().build());

		intricIntegration.deleteFile(MUNICIPALITY_ID, fileId);

		verify(intricClient).deleteFile(fileId);
	}

	@Test
	void deleteFile_exception() {
		var fileId = UUID.randomUUID();

		when(intricClient.deleteFile(fileId)).thenThrow(RuntimeException.class);

		assertThatThrownBy(() -> intricIntegration.deleteFile(MUNICIPALITY_ID, fileId))
			.isInstanceOf(Problem.class)
			.hasMessageContaining("Bad Gateway")
			.hasMessageContaining("Error deleting file with ID: %s".formatted(fileId));

		verify(intricClient).deleteFile(fileId);
	}
}
