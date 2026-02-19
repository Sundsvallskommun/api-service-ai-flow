package se.sundsvall.ai.flow.api;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;
import se.sundsvall.ai.flow.Application;
import se.sundsvall.ai.flow.api.model.ChatRequest;
import se.sundsvall.ai.flow.api.model.CreateSessionRequest;
import se.sundsvall.ai.flow.api.model.RenderRequest;
import se.sundsvall.ai.flow.api.model.SimpleInput;
import se.sundsvall.ai.flow.service.FlowService;
import se.sundsvall.ai.flow.service.SessionService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.zalando.problem.Status.BAD_REQUEST;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class SessionResourceFailureTest {

	private static final String PATH = "/{municipalityId}/session";

	@MockitoBean
	private SessionService sessionService;

	@MockitoBean
	private FlowService flowService;

	@Autowired
	private WebTestClient webTestClient;

	private static Stream<Arguments> createArguments() {
		return Stream.of(
			Arguments.of("666", tuple("createSession.municipalityId", "not a valid municipality ID")));
	}

	private static Stream<Arguments> getSessionArguments() {
		return Stream.of(Arguments.of("666", tuple("getSession.municipalityId", "not a valid municipality ID")));
	}

	private static Stream<Arguments> runSessionArguments() {
		return Stream.of(Arguments.of("666", tuple("runSession.municipalityId", "not a valid municipality ID")));
	}

	private static Stream<Arguments> deleteArguments() {
		return Stream.of(Arguments.of("666", tuple("deleteSession.municipalityId", "not a valid municipality ID")));
	}

	private static Stream<Arguments> runStepArguments() {
		return Stream.of(Arguments.of("666", tuple("runStep.municipalityId", "not a valid municipality ID")));
	}

	private static Stream<Arguments> addSimpleInputArguments() {
		return Stream.of(Arguments.of("666", tuple("addSimpleInputToSession.municipalityId", "not a valid municipality ID")));
	}

	private static Stream<Arguments> addFileInputArguments() {
		return Stream.of(Arguments.of("666", tuple("addFileInputToSession.municipalityId", "not a valid municipality ID")));
	}

	private static Stream<Arguments> clearInputArguments() {
		return Stream.of(Arguments.of("666", tuple("clearInputInSession.municipalityId", "not a valid municipality ID")));
	}

	private static Stream<Arguments> getStepArguments() {
		return Stream.of(Arguments.of("666", tuple("getStep.municipalityId", "not a valid municipality ID")));
	}

	private static Stream<Arguments> generateArguments() {
		return Stream.of(Arguments.of("666", tuple("generateSessionOutput.municipalityId", "not a valid municipality ID")));
	}

	@ParameterizedTest
	@MethodSource("createArguments")
	void createWithInvalidArguments(final String municipalityId, final Tuple expectedResponse) {
		final var response = webTestClient.post().uri(builder -> builder.path(PATH)
			.build(Map.of("municipalityId", municipalityId)))
			.bodyValue(new CreateSessionRequest("fid", null))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations()).extracting(Violation::getField, Violation::getMessage).containsExactly(expectedResponse);

		verifyNoInteractions(sessionService, flowService);
	}

	@ParameterizedTest
	@MethodSource("getSessionArguments")
	void getSessionWithInvalidArguments(final String municipalityId, final Tuple expectedResponse) {
		final var response = webTestClient.get().uri(builder -> builder.path(PATH + "/{sessionId}")
			.build(Map.of("municipalityId", municipalityId, "sessionId", UUID.randomUUID())))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations()).extracting(Violation::getField, Violation::getMessage).containsExactly(expectedResponse);

		verifyNoInteractions(sessionService, flowService);
	}

	@ParameterizedTest
	@MethodSource("runSessionArguments")
	void runSessionWithInvalidArguments(final String municipalityId, final Tuple expectedResponse) {
		final var response = webTestClient.post().uri(builder -> builder.path(PATH + "/{sessionId}")
			.build(Map.of("municipalityId", municipalityId, "sessionId", UUID.randomUUID())))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations()).extracting(Violation::getField, Violation::getMessage).containsExactly(expectedResponse);

		verifyNoInteractions(sessionService, flowService);
	}

	@ParameterizedTest
	@MethodSource("deleteArguments")
	void deleteWithInvalidArguments(final String municipalityId, final Tuple expectedResponse) {
		final var response = webTestClient.delete().uri(builder -> builder.path(PATH + "/{sessionId}")
			.build(Map.of("municipalityId", municipalityId, "sessionId", UUID.randomUUID())))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations()).extracting(Violation::getField, Violation::getMessage).containsExactly(expectedResponse);

		verifyNoInteractions(sessionService, flowService);
	}

	@ParameterizedTest
	@MethodSource("runStepArguments")
	void runStepWithInvalidArguments(final String municipalityId, final Tuple expectedResponse) {
		final var response = webTestClient.post().uri(builder -> builder.path(PATH + "/{sessionId}/step/{stepId}")
			.build(Map.of("municipalityId", municipalityId, "sessionId", UUID.randomUUID(), "stepId", "S1")))
			.bodyValue(new ChatRequest("hi", true))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations()).extracting(Violation::getField, Violation::getMessage).containsExactly(expectedResponse);

		verifyNoInteractions(sessionService, flowService);
	}

	@ParameterizedTest
	@MethodSource("addSimpleInputArguments")
	void addSimpleInputWithInvalidArguments(final String municipalityId, final Tuple expectedResponse) {
		final var response = webTestClient.post().uri(builder -> builder.path(PATH + "/{sessionId}/input/{inputId}/simple")
			.build(Map.of("municipalityId", municipalityId, "sessionId", UUID.randomUUID(), "inputId", "A")))
			.bodyValue(new SimpleInput("v"))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations()).extracting(Violation::getField, Violation::getMessage).containsExactly(expectedResponse);

		verifyNoInteractions(sessionService, flowService);
	}

	@ParameterizedTest
	@MethodSource("addFileInputArguments")
	void addFileInputWithInvalidArguments(final String municipalityId, final Tuple expectedResponse) {
		final MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
		final ByteArrayResource resource = new ByteArrayResource("hello".getBytes()) {
			@Override
			public String getFilename() {
				return "a.txt";
			}
		};
		parts.add("file", resource);

		final var response = webTestClient.post().uri(builder -> builder.path(PATH + "/{sessionId}/input/{inputId}/file")
			.build(Map.of("municipalityId", municipalityId, "sessionId", UUID.randomUUID(), "inputId", "B")))
			.contentType(MediaType.MULTIPART_FORM_DATA)
			.body(BodyInserters.fromMultipartData(parts))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations()).extracting(Violation::getField, Violation::getMessage).containsExactly(expectedResponse);

		verifyNoInteractions(sessionService, flowService);
	}

	@ParameterizedTest
	@MethodSource("clearInputArguments")
	void clearInputWithInvalidArguments(final String municipalityId, final Tuple expectedResponse) {
		final var response = webTestClient.delete().uri(builder -> builder.path(PATH + "/{sessionId}/input/{inputId}").build(Map.of("municipalityId", municipalityId, "sessionId", UUID.randomUUID(), "inputId", "A")))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations()).extracting(Violation::getField, Violation::getMessage).containsExactly(expectedResponse);

		verifyNoInteractions(sessionService, flowService);
	}

	@ParameterizedTest
	@MethodSource("getStepArguments")
	void getStepWithInvalidArguments(final String municipalityId, final Tuple expectedResponse) {
		final var response = webTestClient.get().uri(builder -> builder.path(PATH + "/{sessionId}/step/{stepId}").build(Map.of("municipalityId", municipalityId, "sessionId", UUID.randomUUID(), "stepId", "S1")))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations()).extracting(Violation::getField, Violation::getMessage).containsExactly(expectedResponse);

		verifyNoInteractions(sessionService, flowService);
	}

	@ParameterizedTest
	@MethodSource("generateArguments")
	void generateWithInvalidArguments(final String municipalityId, final Tuple expectedResponse) {
		final var response = webTestClient.post().uri(builder -> builder.path(PATH + "/{sessionId}/generate").build(Map.of("municipalityId", municipalityId, "sessionId", UUID.randomUUID())))
			.bodyValue(new RenderRequest("tpl"))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations()).extracting(Violation::getField, Violation::getMessage).containsExactly(expectedResponse);

		verifyNoInteractions(sessionService, flowService);
	}
}
