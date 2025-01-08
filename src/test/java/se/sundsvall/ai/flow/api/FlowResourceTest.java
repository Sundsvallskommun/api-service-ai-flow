package se.sundsvall.ai.flow.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.Problem;
import se.sundsvall.ai.flow.Application;
import se.sundsvall.ai.flow.api.model.FlowInfo;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("junit")
class FlowResourceTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String BASE_URL = MUNICIPALITY_ID + "/flow";

	@Autowired
	private WebTestClient webTestClient;

	/**
	 * Test that all flows are returned properly.
	 */
	@Test
	void getAllFlows() {
		var result = webTestClient.get().uri(BASE_URL)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBodyList(FlowInfo.class)
			.returnResult()
			.getResponseBody();

		assertThat(result).isNotNull().isNotEmpty();
		assertThat(result.getFirst()).satisfies(flowInfo -> {
			assertThat(flowInfo.id()).isEqualTo("tjansteskrivelse");
			assertThat(flowInfo.name()).isEqualTo("Tjänsteskrivelse");
			assertThat(flowInfo.description()).isEqualTo("Ett Intric AI-flöde för tjänsteskrivelser");
			assertThat(flowInfo.defaultTemplateId()).isEqualTo("ai-mvp.tjansteskrivelse");
		});

	}

	/**
	 * Test a scenario where the given flow id exists
	 */
	@Test
	void getFlow_1() {
		var result = webTestClient.get().uri(BASE_URL + "/tjansteskrivelse")
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(FlowInfo.class)
			.returnResult()
			.getResponseBody();

		assertThat(result).isNotNull();
		assertThat(result).satisfies(flowInfo -> {
			assertThat(flowInfo.id()).isEqualTo("tjansteskrivelse");
			assertThat(flowInfo.name()).isEqualTo("Tjänsteskrivelse");
			assertThat(flowInfo.description()).isEqualTo("Ett Intric AI-flöde för tjänsteskrivelser");
			assertThat(flowInfo.defaultTemplateId()).isEqualTo("ai-mvp.tjansteskrivelse");
		});
	}

	/**
	 * Test a scenario where the given flow id does not exist
	 */
	@Test
	void getFlow_2() {
		var problem = webTestClient.get().uri(BASE_URL + "/bad-flow-id")
			.exchange()
			.expectStatus().isNotFound()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(Problem.class)
			.returnResult().getResponseBody();

		assertThat(problem).isNotNull();
		assertThat(problem.getDetail()).isEqualTo("No flow found with id bad-flow-id");
		assertThat(problem.getStatus()).satisfies(status -> {
			assertThat(status.getStatusCode()).isEqualTo(404);
			assertThat(status.getReasonPhrase()).isEqualTo("Not Found");
		});
	}

}
