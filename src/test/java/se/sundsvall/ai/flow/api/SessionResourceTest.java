package se.sundsvall.ai.flow.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.ai.flow.Application;
import se.sundsvall.ai.flow.api.model.ChatRequest;
import se.sundsvall.ai.flow.api.model.CreateSessionRequest;
import se.sundsvall.ai.flow.api.model.RenderRequest;
import se.sundsvall.ai.flow.api.model.SimpleInput;
import se.sundsvall.ai.flow.model.flowdefinition.Flow;
import se.sundsvall.ai.flow.model.session.Session;
import se.sundsvall.ai.flow.service.FlowService;
import se.sundsvall.ai.flow.service.SessionService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class SessionResourceTest {

	private static final String PATH = "/{municipalityId}/session";

	@MockitoBean
	private SessionService sessionService;

	@MockitoBean
	private FlowService flowService;

	@Autowired
	private WebTestClient webTestClient;

	@AfterEach
	void tearDown() {
		verifyNoMoreInteractions(sessionService, flowService);
	}

	@Test
	void createGetRunAndDeleteSession_happy() {
		final var flow = new Flow().withId("fid");
		final var session = new Session("2281", flow);

		when(flowService.getLatestFlowVersion("fid")).thenReturn(flow);
		when(sessionService.createSession("2281", flow)).thenReturn(session);

		// Create: expect 201 and Location header
		webTestClient.post()
			.uri(builder -> builder.path(PATH).build(Map.of("municipalityId", "2281")))
			.bodyValue(new CreateSessionRequest("fid", null))
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().exists("Location");

		// Get
		when(sessionService.getSession(session.getId())).thenReturn(session);
		webTestClient.get()
			.uri(builder -> builder.path(PATH + "/{sessionId}").build(Map.of("municipalityId", "2281", "sessionId", session.getId())))
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.municipalityId").isEqualTo("2281");

		// Run
		doNothing().when(sessionService).executeSession("2281", session.getId());
		webTestClient.post()
			.uri(builder -> builder.path(PATH + "/{sessionId}").build(Map.of("municipalityId", "2281", "sessionId", session.getId())))
			.exchange()
			.expectStatus().isNoContent();

		// Delete
		doNothing().when(sessionService).deleteSession("2281", session.getId());
		webTestClient.delete()
			.uri(builder -> builder.path(PATH + "/{sessionId}").build(Map.of("municipalityId", "2281", "sessionId", session.getId())))
			.exchange()
			.expectStatus().isNoContent();

		verify(sessionService).getSession(session.getId());
		verify(flowService).getLatestFlowVersion("fid");
		verify(sessionService).createSession("2281", flow);
		verify(sessionService).executeSession("2281", session.getId());
		verify(sessionService).deleteSession("2281", session.getId());
	}

	@Test
	void runStepAndInputsAndRender_happy() {
		final var flow = new Flow().withId("fid");
		final var session = new Session("2281", flow);
		final var stepId = "S1";

		doNothing().when(sessionService).executeStep(eq("2281"), eq(session.getId()), eq(stepId), any(String.class), eq(true));

		// run step
		webTestClient.post()
			.uri(builder -> builder.path(PATH + "/{sessionId}/step/{stepId}").build(Map.of("municipalityId", "2281", "sessionId", session.getId(), "stepId", stepId)))
			.bodyValue(new ChatRequest("hi", true))
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().exists("Location");

		// add simple input
		when(sessionService.addInput(session.getId(), "A", "val")).thenReturn(session);
		webTestClient.post()
			.uri(builder -> builder.path(PATH + "/{sessionId}/input/{inputId}/simple").build(Map.of("municipalityId", "2281", "sessionId", session.getId(), "inputId", "A")))
			.bodyValue(new SimpleInput("val"))
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.id").exists();

		// render
		when(sessionService.renderSession(session.getId(), "tpl", "2281")).thenReturn("rendered");
		webTestClient.post()
			.uri(builder -> builder.path(PATH + "/{sessionId}/generate").build(Map.of("municipalityId", "2281", "sessionId", session.getId())))
			.bodyValue(new RenderRequest("tpl"))
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.data").isEqualTo("rendered");

		verify(sessionService).getSession(session.getId());
		verify(sessionService).renderSession(session.getId(), "tpl", "2281");
		verify(sessionService).addInput(eq(session.getId()), anyString(), anyString());
		verify(sessionService).executeStep(eq("2281"), eq(session.getId()), eq(stepId), anyString(), eq(true));
	}
}
