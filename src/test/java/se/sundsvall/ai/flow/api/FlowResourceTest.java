package se.sundsvall.ai.flow.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static se.sundsvall.ai.flow.TestDataFactory.createFlow;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.Problem;
import se.sundsvall.ai.flow.Application;
import se.sundsvall.ai.flow.api.model.FlowResponse;
import se.sundsvall.ai.flow.api.model.Flows;
import se.sundsvall.ai.flow.integration.db.FlowEntityRepository;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("junit")
class FlowResourceTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String BASE_URL = MUNICIPALITY_ID + "/flow";

	@Autowired
	private FlowEntityRepository flowEntityRepository;

	@Autowired
	private WebTestClient webTestClient;

	/**
	 * Test that all flows are returned properly.
	 */
	@Test
	@Sql(scripts = {
		"/db/scripts/truncate.sql",
		"/db/scripts/testdata.sql"
	})
	void getFlows() {
		var result = webTestClient.get().uri(BASE_URL)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(Flows.class)
			.returnResult()
			.getResponseBody();

		assertThat(result).isNotNull();
		assertThat(result.flows().getFirst()).satisfies(flow -> {
			assertThat(flow.name()).isEqualTo("Tjänsteskrivelse");
			assertThat(flow.version()).isEqualTo(1);
		});

	}

	/**
	 * Test a scenario where the given flow id exists
	 */
	@Test
	@Sql(scripts = {
		"/db/scripts/truncate.sql",
		"/db/scripts/testdata.sql"
	})
	void getFlowByNameAndVersion_1() {
		var result = webTestClient.get().uri(BASE_URL + "/tjansteskrivelse/1")
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(FlowResponse.class)
			.returnResult()
			.getResponseBody();

		assertThat(result).isNotNull().satisfies(flow -> {
			assertThat(flow.name()).isEqualTo("Tjänsteskrivelse");
			assertThat(flow.version()).isEqualTo(1);
		});
	}

	/**
	 * Test a scenario where the given flow id does not exist
	 */
	@Test
	@Sql(scripts = {
		"/db/scripts/truncate.sql",
		"/db/scripts/testdata.sql"
	})
	void getFlowByNameAndVersion_2() {
		var problem = webTestClient.get().uri(BASE_URL + "/bad-flow-id/5")
			.exchange()
			.expectStatus().isNotFound()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(Problem.class)
			.returnResult().getResponseBody();

		assertThat(problem).isNotNull();
		assertThat(problem.getDetail()).isEqualTo("No flow found with name bad-flow-id and version 5");
		assertThat(problem.getStatus()).satisfies(status -> {
			assertThat(status.getStatusCode()).isEqualTo(404);
			assertThat(status.getReasonPhrase()).isEqualTo("Not Found");
		});
	}

	@Test
	@Sql(scripts = {
		"/db/scripts/truncate.sql",
		"/db/scripts/testdata.sql"
	})
	void deleteFlow_1() {
		webTestClient.delete().uri(BASE_URL + "/tjansteskrivelse/1")
			.exchange()
			.expectStatus().isOk();

		var flows = flowEntityRepository.findAll();
		assertThat(flows).isEmpty();
	}

	@Test
	@Sql(scripts = {
		"/db/scripts/truncate.sql",
		"/db/scripts/testdata.sql"
	})
	void deleteFlow_2() {
		webTestClient.delete().uri(BASE_URL + "/tjansteskrivelse/5")
			.exchange()
			.expectStatus().isNotFound();

		var flows = flowEntityRepository.findAll();
		assertThat(flows).hasSize(1);
	}

	@Test
	@Sql("/db/scripts/truncate.sql")
	void createFlow_1() {
		var flow = createFlow();

		webTestClient.post().uri(BASE_URL)
			.bodyValue(flow)
			.exchange()
			.expectStatus().isCreated();

		var flows = flowEntityRepository.findAll();
		assertThat(flows).hasSize(1);
	}

}
