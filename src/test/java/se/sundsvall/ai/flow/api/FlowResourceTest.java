package se.sundsvall.ai.flow.api;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;
import se.sundsvall.ai.flow.Application;
import se.sundsvall.ai.flow.api.model.FlowSummary;
import se.sundsvall.ai.flow.model.flowdefinition.Flow;
import se.sundsvall.ai.flow.service.FlowService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.ALL;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.zalando.problem.Status.BAD_REQUEST;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class FlowResourceTest {

	private static final String PATH = "/{municipalityId}/flow";
	private static final String MUNICIPALITY_ID = "2281";

	@MockitoBean
	private FlowService flowService;

	@Autowired
	private WebTestClient webTestClient;

	private static Stream<Arguments> listFlowsArguments() {
		return Stream.of(
			Arguments.of("666", tuple("getFlows.municipalityId", "not a valid municipality ID")));
	}

	private static Stream<Arguments> getLatestArguments() {
		return Stream.of(
			Arguments.of("666", "fid", tuple("getLatestFlowVersionById.municipalityId", "not a valid municipality ID")));
	}

	private static Stream<Arguments> getByVersionArguments() {
		return Stream.of(
			Arguments.of("666", "fid", tuple("getFlowByIdAndVersion.municipalityId", "not a valid municipality ID")));
	}

	private static Stream<Arguments> deleteArguments() {
		return Stream.of(
			Arguments.of("666", "fid", tuple("deleteFlow.municipalityId", "not a valid municipality ID")));
	}

	private static Stream<Arguments> deleteVersionArguments() {
		return Stream.of(
			Arguments.of("666", "fid", 1, tuple("deleteFlowVersion.municipalityId", "not a valid municipality ID")));
	}

	private static Stream<Arguments> createArguments() {
		return Stream.of(
			Arguments.of("666", tuple("createFlow.municipalityId", "not a valid municipality ID")));
	}

	@AfterEach
	void tearDown() {
		verifyNoMoreInteractions(flowService);
	}

	@Test
	void listFlows() {
		when(flowService.getFlows()).thenReturn(List.of(new FlowSummary("id1", 1, "n", "d")));

		webTestClient.get()
			.uri(builder -> builder.path(PATH).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(FlowSummary[].class)
			.value(flowSummary -> assertThat(flowSummary[0].id()).isEqualTo("id1"));

		verify(flowService).getFlows();
	}

	@Test
	void getLatest_ok_and_notFound() {
		when(flowService.getLatestFlowVersion("fid")).thenReturn(new Flow().withId("fid").withVersion(2));

		webTestClient.get()
			.uri(builder -> builder.path(PATH + "/{flowId}").build(Map.of("municipalityId", MUNICIPALITY_ID, "flowId", "fid")))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(Flow.class)
			.value(flow -> {
				assertThat(flow.getId()).isEqualTo("fid");
				assertThat(flow.getVersion()).isEqualTo(2);
			});

		when(flowService.getLatestFlowVersion("missing")).thenThrow(Problem.valueOf(Status.NOT_FOUND));

		webTestClient.get()
			.uri(builder -> builder.path(PATH + "/{flowId}").build(Map.of("municipalityId", MUNICIPALITY_ID, "flowId", "missing")))
			.exchange()
			.expectStatus().isNotFound();

		verify(flowService).getLatestFlowVersion("fid");
		verify(flowService).getLatestFlowVersion("missing");
	}

	@Test
	void getByVersion_ok_and_notFound() {
		when(flowService.getFlowVersion("fid", 3)).thenReturn(new Flow().withId("fid").withVersion(3));

		webTestClient.get()
			.uri(builder -> builder.path(PATH + "/{flowId}/{version}").build(Map.of("municipalityId", MUNICIPALITY_ID, "flowId", "fid", "version", 3)))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(Flow.class)
			.value(flow -> assertThat(flow.getVersion()).isEqualTo(3));

		when(flowService.getFlowVersion("fid", 99)).thenThrow(Problem.valueOf(Status.NOT_FOUND));

		webTestClient.get()
			.uri(builder -> builder.path(PATH + "/{flowId}/{version}").build(Map.of("municipalityId", MUNICIPALITY_ID, "flowId", "fid", "version", 99)))
			.exchange()
			.expectStatus().isNotFound();

		verify(flowService, times(2)).getFlowVersion(eq("fid"), any());
	}

	@Test
	void deleteFlow_and_deleteVersion() {
		webTestClient.delete()
			.uri(builder -> builder.path(PATH + "/{flowId}").build(Map.of("municipalityId", MUNICIPALITY_ID, "flowId", "fid")))
			.exchange()
			.expectStatus().isOk();
		verify(flowService).deleteFlow("fid");

		webTestClient.delete()
			.uri(builder -> builder.path(PATH + "/{flowId}/{version}").build(Map.of("municipalityId", MUNICIPALITY_ID, "flowId", "fid", "version", 2)))
			.exchange()
			.expectStatus().isOk();
		verify(flowService).deleteFlowVersion("fid", 2);
	}

	@Test
	void createFlow_createdWithLocation() {
		final var flow = new Flow().withId("fid").withVersion(5);
		when(flowService.createFlow(any(Flow.class))).thenReturn(flow);

		webTestClient.post()
			.uri(builder -> builder.path(PATH).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.contentType(APPLICATION_JSON)
			.bodyValue(flow)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(ALL)
			.expectHeader().location("/fid/5")
			.expectBody().isEmpty();

		verify(flowService).createFlow(any(Flow.class));
	}

	@ParameterizedTest
	@MethodSource("listFlowsArguments")
	void listFlowsWithInvalidArguments(final String municipalityId, final Tuple expectedResponse) {
		final var response = webTestClient.get()
			.uri(builder -> builder.path(PATH).build(Map.of("municipalityId", municipalityId)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations()).extracting(Violation::getField, Violation::getMessage).containsExactly(expectedResponse);

		verifyNoInteractions(flowService);
	}

	@ParameterizedTest
	@MethodSource("getLatestArguments")
	void getLatestWithInvalidArguments(final String municipalityId, final String flowId, final Tuple expectedResponse) {
		final var response = webTestClient.get()
			.uri(builder -> builder.path(PATH + "/{flowId}").build(Map.of("municipalityId", municipalityId, "flowId", flowId)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations()).extracting(Violation::getField, Violation::getMessage).containsExactly(expectedResponse);

		verifyNoInteractions(flowService);
	}

	@ParameterizedTest
	@MethodSource("getByVersionArguments")
	void getByVersionWithInvalidArguments(final String municipalityId, final String flowId, final Tuple expectedResponse) {
		final var response = webTestClient.get()
			.uri(builder -> builder.path(PATH + "/{flowId}/{version}").build(Map.of("municipalityId", municipalityId, "flowId", flowId, "version", 1)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations()).extracting(Violation::getField, Violation::getMessage).containsExactly(expectedResponse);

		verifyNoInteractions(flowService);
	}

	@ParameterizedTest
	@MethodSource("deleteArguments")
	void deleteWithInvalidArguments(final String municipalityId, final String flowId, final Tuple expectedResponse) {
		final var response = webTestClient.delete()
			.uri(builder -> builder.path(PATH + "/{flowId}").build(Map.of("municipalityId", municipalityId, "flowId", flowId)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations()).extracting(Violation::getField, Violation::getMessage).containsExactly(expectedResponse);

		verifyNoInteractions(flowService);
	}

	@ParameterizedTest
	@MethodSource("deleteVersionArguments")
	void deleteVersionWithInvalidArguments(final String municipalityId, final String flowId, final Integer version, final Tuple expectedResponse) {
		final var response = webTestClient.delete()
			.uri(builder -> builder.path(PATH + "/{flowId}/{version}").build(Map.of("municipalityId", municipalityId, "flowId", flowId, "version", version)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations()).extracting(Violation::getField, Violation::getMessage).containsExactly(expectedResponse);

		verifyNoInteractions(flowService);
	}

	@ParameterizedTest
	@MethodSource("createArguments")
	void createWithInvalidArguments(final String municipalityId, final Tuple expectedResponse) {
		final var response = webTestClient.post()
			.uri(builder -> builder.path(PATH).build(Map.of("municipalityId", municipalityId)))
			.bodyValue(new Flow())
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations()).extracting(Violation::getField, Violation::getMessage).containsExactly(expectedResponse);

		verifyNoInteractions(flowService);
	}
}
