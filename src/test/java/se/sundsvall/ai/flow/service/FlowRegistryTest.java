package se.sundsvall.ai.flow.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static se.sundsvall.ai.flow.TestDataFactory.createFlow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import se.sundsvall.ai.flow.Application;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("junit")
class FlowRegistryTest {

	@Autowired
	private FlowRegistry flowRegistry;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(flowRegistry, "flows", new ArrayList<>());
	}

	@Test
	void initialize() throws IOException {
		assertThat(flowRegistry.getAllFlows()).isEmpty();

		flowRegistry.initialize();

		assertThat(flowRegistry.getAllFlows()).isNotEmpty();
	}

	/**
	 * Test scenario where flows is empty
	 */
	@Test
	void getAllFlows_1() {
		var result = flowRegistry.getAllFlows();

		assertThat(result).isEmpty();
	}

	/**
	 * Test scenario where the flows are not empty.
	 */
	@Test
	void getAllFlows_2() {
		ReflectionTestUtils.setField(flowRegistry, "flows", List.of(createFlow()));
		var result = flowRegistry.getAllFlows();

		assertThat(result).isNotEmpty().hasSize(1);
	}

	/**
	 * Test scenario where the flow id exists
	 */
	@Test
	void getFlow_1() {
		ReflectionTestUtils.setField(flowRegistry, "flows", List.of(createFlow()));
		var result = flowRegistry.getFlow("tjansteskrivelse");

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo("tjansteskrivelse");
		assertThat(result.getName()).isEqualTo("Tjänsteskrivelse");
		assertThat(result.getDescription()).isEqualTo("Ett Intric AI-flöde för tjänsteskrivelser");
		assertThat(result.getDefaultTemplateId()).isEqualTo("ai-mvp.tjansteskrivelse");
	}

	/**
	 * Test scenario where the flow id does not exist
	 */
	@Test
	void getFlow_2() {
		assertThatThrownBy(() -> flowRegistry.getFlow("tjansteskrivelse"))
			.isInstanceOf(Problem.class)
			.hasFieldOrPropertyWithValue("status", Status.NOT_FOUND)
			.hasFieldOrPropertyWithValue("detail", "No flow found with id tjansteskrivelse");
	}

}
