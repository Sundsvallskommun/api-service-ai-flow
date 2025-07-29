package se.sundsvall.ai.flow.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.ALL;

import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.ai.flow.Application;
import se.sundsvall.ai.flow.api.model.instance.Instance;
import se.sundsvall.ai.flow.service.InstanceService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class InstanceResourceTest {

	private static final String PATH = "/{municipalityId}/instances";

	@Autowired
	private WebTestClient webTestClient;

	@MockitoBean
	private InstanceService instanceService;

	@Test
	void createInstance() {
		// Arrange
		final var municipalityId = "2281";
		final var instanceId = "1234";
		final var baseUrl = "https://example.com";
		final var username = "user123";
		final var password = "pass123";
		final var connectTimeout = 5;
		final var readTimeout = 60;

		final var instance = Instance.create()
			.withBaseUrl(baseUrl)
			.withUsername(username)
			.withPassword(password)
			.withConnectTimeout(connectTimeout)
			.withReadTimeout(readTimeout);

		when(instanceService.createInstance(municipalityId, instance)).thenReturn(instanceId);

		// Act
		webTestClient.post()
			.uri(builder -> builder.path(PATH).build(Map.of("municipalityId", municipalityId)))
			.bodyValue(instance)
			.exchange()
			.expectStatus().isOk()
			.expectBody().isEmpty();

		// Assert
		verify(instanceService).createInstance(municipalityId, instance);
		verifyNoMoreInteractions(instanceService);
	}

	@Test
	void deleteInstance() {
		// Arrange
		final var municipalityId = "2281";
		final var instanceId = UUID.randomUUID().toString();

		// Act
		webTestClient.delete()
			.uri(builder -> builder.path(PATH + "/{instanceId}").build(Map.of("municipalityId", municipalityId, "instanceId", instanceId)))
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().contentType(ALL)
			.expectBody().isEmpty();

		// Assert
		verify(instanceService).deleteInstance(municipalityId, instanceId);
		verifyNoMoreInteractions(instanceService);
	}
}
