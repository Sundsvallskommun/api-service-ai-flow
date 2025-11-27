package se.sundsvall.ai.flow.service.execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import se.sundsvall.ai.flow.integration.eneo.EneoService;
import se.sundsvall.ai.flow.integration.eneo.model.Response;
import se.sundsvall.ai.flow.model.flowdefinition.Step;
import se.sundsvall.ai.flow.model.session.Session;
import se.sundsvall.ai.flow.model.session.StepExecutionFactory;

class AssistantTargetExecutorTest {

	@Test
	void supportsAndExecuteNewSession() {
		final var eneoService = mock(EneoService.class);
		final var executor = new AssistantTargetExecutor(eneoService);

		assertThat(executor.supports(Step.Target.Type.ASSISTANT)).isTrue();

		final var step = new Step().withId("S1").withName("S1").withTarget(new Step.Target(Step.Target.Type.ASSISTANT, UUID.randomUUID()));
		final var flow = new se.sundsvall.ai.flow.model.flowdefinition.Flow().withSteps(List.of(step));
		final var session = new Session("2281", flow, new StepExecutionFactory());
		final var exec = session.getStepExecution("S1");

		final var expectedSessionId = UUID.randomUUID();
		when(eneoService.askAssistant("2281", step.getTarget().id(), List.of(), "")).thenReturn(new Response(expectedSessionId, "ans"));

		final var ctx = new StepRunContext("2281", session, exec, List.of(), List.of(), "", null, true);
		final var result = executor.execute(ctx);

		// call the methods multiple times to ensure coverage for the anonymous inner class
		assertThat(result.output()).isEqualTo("ans");
		assertThat(result.output()).isEqualTo("ans");
		// sessionId should be the one returned by the response
		assertThat(result.sessionId()).isEqualTo(expectedSessionId);
		assertThat(result.sessionId()).isEqualTo(expectedSessionId);
		// runId is not used for ASSISTANT - should be null
		assertThat(result.runId()).isNull();
		assertThat(result.runId()).isNull();
	}

	@Test
	void executeFollowupUsesIntricSessionId() {
		final var eneoService = mock(EneoService.class);
		final var executor = new AssistantTargetExecutor(eneoService);

		final var step = new Step().withId("S1").withName("S1").withTarget(new Step.Target(Step.Target.Type.ASSISTANT, UUID.randomUUID()));
		final var flow = new se.sundsvall.ai.flow.model.flowdefinition.Flow().withSteps(List.of(step));
		final var session = new Session("2281", flow, new StepExecutionFactory());
		final var exec = session.getStepExecution("S1");
		exec.setIntricSessionId(UUID.randomUUID());

		when(eneoService.askAssistantFollowup("2281", step.getTarget().id(), exec.getIntricSessionId(), List.of(), "", "q")).thenReturn(new Response("follow"));

		final var ctx = new StepRunContext("2281", session, exec, List.of(), List.of(), "", "q", true);
		final var result = executor.execute(ctx);

		assertThat(result.output()).isEqualTo("follow");
		assertThat(result.output()).isEqualTo("follow");
		assertThat(result.sessionId()).isEqualTo(exec.getIntricSessionId());
		assertThat(result.sessionId()).isEqualTo(exec.getIntricSessionId());
		// runId is not used for ASSISTANT follow-up - should be null
		assertThat(result.runId()).isNull();
		assertThat(result.runId()).isNull();
	}
}
