package se.sundsvall.ai.flow.service.execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import se.sundsvall.ai.flow.integration.eneo.EneoService;
import se.sundsvall.ai.flow.integration.eneo.model.Response;
import se.sundsvall.ai.flow.model.flowdefinition.Flow;
import se.sundsvall.ai.flow.model.flowdefinition.Step;
import se.sundsvall.ai.flow.model.session.Session;
import se.sundsvall.ai.flow.model.session.StepExecutionFactory;

class ServiceTargetExecutorTest {

	@Test
	void supportsAndExecute() {
		final var eneoService = mock(EneoService.class);
		final var executor = new ServiceTargetExecutor(eneoService);

		assertThat(executor.supports(Step.Target.Type.SERVICE)).isTrue();
		assertThat(executor.supports(Step.Target.Type.APP)).isFalse();

		final var step = new Step().withId("S1").withName("S1").withTarget(new Step.Target(Step.Target.Type.SERVICE, UUID.randomUUID()));
		final var flow = new Flow().withSteps(List.of(step));
		final var session = new Session("2281", flow, new StepExecutionFactory());
		final var stepExecution = session.getStepExecution("S1");

		when(eneoService.runService("2281", step.getTarget().id(), List.of(), "", "question")).thenReturn(new Response("answer"));

		final var stepRunContext = new StepRunContext("2281", session, stepExecution, List.of(), List.of(), "", "question", true);
		final var result = executor.execute(stepRunContext);

		assertThat(result.output()).isEqualTo("answer");
		assertThat(result.runId()).isNull();
		assertThat(result.sessionId()).isNull();
	}
}
