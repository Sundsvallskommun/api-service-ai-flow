package se.sundsvall.ai.flow.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static se.sundsvall.ai.flow.TestDataFactory.createNewSession;
import static se.sundsvall.ai.flow.TestDataFactory.createRenderRequest;
import static se.sundsvall.ai.flow.TestDataFactory.createSessionWithStepExecutions;
import static se.sundsvall.ai.flow.TestDataFactory.createStepExecution;
import static se.sundsvall.ai.flow.service.flow.ExecutionState.RUNNING;

import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import se.sundsvall.ai.flow.Application;
import se.sundsvall.ai.flow.api.model.Input;
import se.sundsvall.ai.flow.api.model.Output;
import se.sundsvall.ai.flow.model.Session;
import se.sundsvall.ai.flow.service.SessionService;
import se.sundsvall.ai.flow.service.StepExecutor;
import se.sundsvall.ai.flow.service.flow.ExecutionState;
import se.sundsvall.ai.flow.service.flow.StepExecution;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("junit")
class SessionResourceTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String BASE_URL = MUNICIPALITY_ID + "/session";

	@Autowired
	private WebTestClient webTestClient;

	@MockitoBean
	private SessionService sessionServiceMock;

	@MockitoBean
	private StepExecutor stepExecutorMock;

	/**
	 * Test scenario where a session is found and returned.
	 */
	@Test
	void getSession_1() {
		var sessionId = UUID.randomUUID();
		var session = createNewSession();
		when(sessionServiceMock.getSession(sessionId)).thenReturn(session);

		var result = webTestClient.get().uri(BASE_URL + "/{sessionId}", sessionId)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(Session.class)
			.returnResult()
			.getResponseBody();

		assertThat(result).isNotNull();
		// flow is annotated with @JsonIgnore and therefore should be null.
		assertThat(result.getFlow()).isNull();

		assertThat(result.getId()).isEqualTo(session.getId());
		assertThat(result.getState()).isEqualTo(session.getState());
		assertThat(result.getTokenCount()).isEqualTo(session.getTokenCount());
		assertThat(result.getInput()).isEqualTo(session.getInput());

		verify(sessionServiceMock).getSession(sessionId);
		verifyNoMoreInteractions(sessionServiceMock);
	}

	/**
	 * Test scenario where no session is found and a Problem is returned.
	 */
	@Test
	void getSession_2() {
		var sessionId = UUID.randomUUID();
		doThrow(Problem.valueOf(Status.NOT_FOUND, "Session with id: %s not found".formatted(sessionId)))
			.when(sessionServiceMock).getSession(sessionId);

		var problem = webTestClient.get().uri(BASE_URL + "/{sessionId}", sessionId)
			.exchange()
			.expectStatus().isNotFound()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		assertThat(problem).isNotNull();
		assertThat(problem.getDetail()).isEqualTo("Session with id: %s not found".formatted(sessionId));
		assertThat(problem.getStatus()).satisfies(status -> {
			assertThat(status.getStatusCode()).isEqualTo(404);
			assertThat(status.getReasonPhrase()).isEqualTo("Not Found");
		});

		verify(sessionServiceMock).getSession(sessionId);
		verifyNoMoreInteractions(sessionServiceMock);
	}

	/**
	 * Test scenario where a session is created and returned.
	 */
	@Test
	void createSession_1() {
		var flowName = "flowId";
		var version = 1;
		var session = createNewSession();
		when(sessionServiceMock.createSession(flowName, version)).thenReturn(session);

		var result = webTestClient.post().uri(BASE_URL + "/{flowName}/{version}", flowName, version)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().location("/session/" + session.getId())
			.expectBody(Session.class)
			.returnResult()
			.getResponseBody();

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(session.getId());
		assertThat(result.getState()).isEqualTo(session.getState());
		assertThat(result.getTokenCount()).isEqualTo(session.getTokenCount());
		assertThat(result.getInput()).isEqualTo(session.getInput());

		verify(sessionServiceMock).createSession(flowName, version);
		verifyNoMoreInteractions(sessionServiceMock);
	}

	/**
	 * Test scenario where an input is added to a session.
	 */
	@Test
	void addInput_1() {
		var sessionId = UUID.randomUUID();
		var input = new Input("uppdraget-till-tjansten", Base64.getEncoder().encodeToString("newValue".getBytes()));
		var session = createNewSession();

		when(sessionServiceMock.getSession(sessionId)).thenReturn(session);
		when(sessionServiceMock.addInput(sessionId, input.inputId(), input.value())).thenCallRealMethod();

		var result = webTestClient.post().uri(BASE_URL + "/{sessionId}", sessionId)
			.bodyValue(input)
			.exchange()
			.expectStatus().isOk()
			.expectBody(Session.class)
			.returnResult()
			.getResponseBody();

		assertThat(result).isNotNull();
		assertThat(result.getInput()).containsEntry(input.inputId(), List.of("newValue"));
		assertThat(result.getId()).isEqualTo(session.getId());
		assertThat(result.getState()).isEqualTo(session.getState());
		assertThat(result.getTokenCount()).isEqualTo(session.getTokenCount());
		assertThat(result.getInput()).isEqualTo(session.getInput());

		verify(sessionServiceMock).getSession(sessionId);
		verify(sessionServiceMock).addInput(sessionId, input.inputId(), input.value());
		verifyNoMoreInteractions(sessionServiceMock);
	}

	/**
	 * Test scenario where there is no flow matching the input id.
	 */
	@Test
	void addInput_2() {
		var sessionId = UUID.randomUUID();
		var input = new Input("invalid-id", Base64.getEncoder().encodeToString("value1".getBytes()));
		var session = createNewSession();

		when(sessionServiceMock.getSession(sessionId)).thenReturn(session);
		when(sessionServiceMock.addInput(sessionId, input.inputId(), input.value())).thenCallRealMethod();

		var problem = webTestClient.post().uri(BASE_URL + "/{sessionId}", sessionId)
			.bodyValue(input)
			.exchange()
			.expectStatus().isNotFound()
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		assertThat(problem).isNotNull();
		assertThat(problem.getDetail()).isEqualTo("No input '%s' exists in flow '%s'".formatted("invalid-id", session.getFlow().getName()));
		assertThat(problem.getStatus()).satisfies(status -> {
			assertThat(status.getStatusCode()).isEqualTo(404);
			assertThat(status.getReasonPhrase()).isEqualTo("Not Found");
		});

		verify(sessionServiceMock).getSession(sessionId);
		verify(sessionServiceMock).addInput(sessionId, input.inputId(), input.value());
		verifyNoMoreInteractions(sessionServiceMock);
	}

	/**
	 * Test scenario where the input is replaced by the new value.
	 */
	@Test
	void replaceInput_1() {
		var sessionId = UUID.randomUUID();
		var input = new Input("arendenummer", Base64.getEncoder().encodeToString("new value".getBytes()));
		var session = createNewSession();

		when(sessionServiceMock.getSession(sessionId)).thenReturn(session);
		when(sessionServiceMock.replaceInput(sessionId, input.inputId(), input.value())).thenCallRealMethod();

		var result = webTestClient.put().uri(BASE_URL + "/{sessionId}", sessionId)
			.bodyValue(input)
			.exchange()
			.expectStatus().isOk()
			.expectBody(Session.class)
			.returnResult()
			.getResponseBody();

		assertThat(result).isNotNull();
		assertThat(result.getInput()).containsEntry(input.inputId(), List.of("new value"));
		assertThat(result.getId()).isEqualTo(session.getId());
		assertThat(result.getState()).isEqualTo(session.getState());
		assertThat(result.getTokenCount()).isEqualTo(session.getTokenCount());
		assertThat(result.getInput()).isEqualTo(session.getInput());

		verify(sessionServiceMock).getSession(sessionId);
		verify(sessionServiceMock).replaceInput(sessionId, input.inputId(), input.value());
		verifyNoMoreInteractions(sessionServiceMock);
	}

	@Test
	void replaceInput_2() {
		var sessionId = UUID.randomUUID();
		var input = new Input("invalid-id", Base64.getEncoder().encodeToString("value1".getBytes()));
		var session = createNewSession();

		when(sessionServiceMock.getSession(sessionId)).thenReturn(session);
		when(sessionServiceMock.replaceInput(sessionId, input.inputId(), input.value())).thenCallRealMethod();

		var problem = webTestClient.put().uri(BASE_URL + "/{sessionId}", sessionId)
			.bodyValue(input)
			.exchange()
			.expectStatus().isNotFound()
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		assertThat(problem).isNotNull();
		assertThat(problem.getDetail()).isEqualTo("No input '%s' exists in flow '%s'".formatted("invalid-id", session.getFlow().getName()));
		assertThat(problem.getStatus()).satisfies(status -> {
			assertThat(status.getStatusCode()).isEqualTo(404);
			assertThat(status.getReasonPhrase()).isEqualTo("Not Found");
		});

		verify(sessionServiceMock).getSession(sessionId);
		verify(sessionServiceMock).replaceInput(sessionId, input.inputId(), input.value());
		verifyNoMoreInteractions(sessionServiceMock);
	}

	/**
	 * Test scenario where a step execution is found and returned.
	 */
	@Test
	void getStepExecution_1() {
		var sessionId = UUID.randomUUID();
		var session = createSessionWithStepExecutions();
		when(sessionServiceMock.getSession(sessionId)).thenReturn(session);

		var result = webTestClient.get().uri(BASE_URL + "/{sessionId}/{stepId}", sessionId, "step")
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(StepExecution.class)
			.returnResult()
			.getResponseBody();

		assertThat(result).isNotNull();

		// Also annotated with @JsonIgnore but is set explicitly. Is excluded in the response but is not null.
		assertThat(result.getId()).isNotNull();
		// Annotated with @JsonIgnore and therefore should be null.
		assertThat(result.getSessionId()).isNull();
		// Annotated with @JsonIgnore and therefore should be null.
		assertThat(result.getStep()).isNull();
		// Annotated with @JsonIgnore and therefore should be null.
		assertThat(result.getRequiredStepExecutions()).isNull();

		assertThat(result.getOutput()).isEqualTo("output");
		assertThat(result.getState()).isEqualTo(ExecutionState.PENDING);
		assertThat(result.getStartedAt()).isNotNull();

		verify(sessionServiceMock).getSession(sessionId);
		verifyNoMoreInteractions(sessionServiceMock);
	}

	/**
	 * Test scenario where no step execution is found for given stepId.
	 */
	@Test
	void getStepExecution_2() {
		var sessionId = UUID.randomUUID();
		var session = createNewSession();
		when(sessionServiceMock.getSession(sessionId)).thenReturn(session);

		var problem = webTestClient.get().uri(BASE_URL + "/{sessionId}/{stepId}", sessionId, "invalid-step")
			.exchange()
			.expectStatus().isNotFound()
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		assertThat(problem).isNotNull();
		assertThat(problem.getDetail()).isEqualTo("No step execution exists for step '%s' in flow '%s' for session %s".formatted("invalid-step", session.getFlow().getName(), sessionId));
		assertThat(problem.getStatus()).satisfies(status -> {
			assertThat(status.getStatusCode()).isEqualTo(404);
			assertThat(status.getReasonPhrase()).isEqualTo("Not Found");
		});
	}

	/**
	 * Test scenario where the step is successfully started.
	 */
	@Test
	void runStep_1() {
		var sessionId = UUID.randomUUID();
		var stepId = "arendet";
		var session = createNewSession();
		when(sessionServiceMock.getSession(sessionId)).thenReturn(session);
		when(sessionServiceMock.getStep(sessionId, stepId)).thenCallRealMethod();
		when(sessionServiceMock.createStepExecution(sessionId, stepId)).thenCallRealMethod();

		var result = webTestClient.post().uri(BASE_URL + "/run/{sessionId}/{stepId}", sessionId, stepId)
			.exchange()
			.expectStatus().isCreated()
			.expectBody(StepExecution.class)
			.returnResult()
			.getResponseBody();

		assertThat(result).isNotNull();
		assertThat(result.getState()).isEqualTo(ExecutionState.PENDING);

		verify(sessionServiceMock, times(3)).getSession(sessionId);
		verify(sessionServiceMock).createStepExecution(sessionId, stepId);
		verify(stepExecutorMock).executeStep(any());

	}

	/**
	 * Test scenario where the step is already running.
	 */
	@Test
	void runStep_2() {
		var sessionId = UUID.randomUUID();
		var stepId = "arendet";
		var session = createNewSession();
		session.addStepExecution(stepId, createStepExecution().withState(RUNNING));
		when(sessionServiceMock.getSession(sessionId)).thenReturn(session);

		var problem = webTestClient.post().uri(BASE_URL + "/run/{sessionId}/{stepId}", sessionId, stepId)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		assertThat(problem).isNotNull();
		assertThat(problem.getDetail()).isEqualTo("Unable to run already running step '%s' in flow '%s' for session %s".formatted(stepId, session.getFlow().getName(), sessionId));
		assertThat(problem.getStatus()).satisfies(status -> {
			assertThat(status.getStatusCode()).isEqualTo(400);
			assertThat(status.getReasonPhrase()).isEqualTo("Bad Request");
		});

		verify(sessionServiceMock).getSession(sessionId);
		verifyNoMoreInteractions(sessionServiceMock);
	}

	/**
	 * Test scenario where the session does not have the required input for the step.
	 */
	@Test
	void runStep_3() {
		var sessionId = UUID.randomUUID();
		var stepId = "arendet";
		var session = createNewSession();
		session.getInput().clear();

		when(sessionServiceMock.getSession(sessionId)).thenReturn(session);
		when(sessionServiceMock.getStep(sessionId, stepId)).thenCallRealMethod();
		when(sessionServiceMock.createStepExecution(sessionId, stepId)).thenCallRealMethod();

		var problem = webTestClient.post().uri(BASE_URL + "/run/{sessionId}/{stepId}", sessionId, stepId)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		assertThat(problem).isNotNull();
		assertThat(problem.getDetail()).isEqualTo("Required input 'uppdraget-till-tjansten' is unset for step 'Ärendet' in flow 'Tjänsteskrivelse' for session %s".formatted(sessionId));
		assertThat(problem.getStatus()).satisfies(status -> {
			assertThat(status.getStatusCode()).isEqualTo(400);
			assertThat(status.getReasonPhrase()).isEqualTo("Bad Request");
		});

		verify(sessionServiceMock, times(3)).getSession(sessionId);
		verify(sessionServiceMock).createStepExecution(sessionId, stepId);
		verify(sessionServiceMock).getStep(sessionId, stepId);
		verifyNoMoreInteractions(sessionServiceMock);
	}

	/**
	 * Test scenario where the session output is generated.
	 */
	@Test
	void generateSessionOutput_1() {
		var sessionId = UUID.randomUUID();
		var renderRequest = createRenderRequest();
		var session = createNewSession();

		when(sessionServiceMock.getSession(sessionId)).thenReturn(session);
		when(sessionServiceMock.renderSession(sessionId, renderRequest.templateId(), MUNICIPALITY_ID)).thenReturn("output");

		var result = webTestClient.post().uri(BASE_URL + "/{sessionId}/generate", sessionId)
			.bodyValue(renderRequest)
			.exchange()
			.expectStatus().isOk()
			.expectBody(Output.class)
			.returnResult()
			.getResponseBody();

		assertThat(result).isNotNull();
		assertThat(result.data()).isEqualTo("output");

		verify(sessionServiceMock).getSession(sessionId);
		verify(sessionServiceMock).renderSession(sessionId, renderRequest.templateId(), MUNICIPALITY_ID);
		verifyNoMoreInteractions(sessionServiceMock);

	}

}
