package se.sundsvall.ai.flow.service.execution;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import se.sundsvall.ai.flow.integration.eneo.EneoService;
import se.sundsvall.ai.flow.integration.eneo.model.Response;
import se.sundsvall.ai.flow.model.flowdefinition.Flow;
import se.sundsvall.ai.flow.model.flowdefinition.Step;
import se.sundsvall.ai.flow.model.session.Session;
import se.sundsvall.ai.flow.model.session.StepExecutionFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AppTargetExecutorTest {

	@Test
	void supportsAndExecuteNewRun() throws Exception {
		final var eneoService = mock(EneoService.class);
		final var poller = mock(AppRunPoller.class);
		final var executor = new AppTargetExecutor(eneoService, poller);

		assertThat(executor.supports(Step.Target.Type.APP)).isTrue();

		final var step = new Step().withId("S1").withName("S1").withTarget(new Step.Target(Step.Target.Type.APP, UUID.randomUUID()));
		final var flow = new Flow().withSteps(List.of(step));
		final var session = new Session("2281", flow, new StepExecutionFactory());
		final var exec = session.getStepExecution("S1");

		final var runId = UUID.randomUUID();
		when(eneoService.runApp("2281", step.getTarget().id(), List.of())).thenReturn(new Response(runId, "ignored", null));
		when(poller.pollUntilComplete("2281", runId, step.getName())).thenReturn("done");

		final var stepRunContext = new StepRunContext("2281", session, exec, List.of(), List.of(), "", null, true);
		final var result = executor.execute(stepRunContext);

		assertThat(result.output()).isEqualTo("done");
		assertThat(result.runId()).isEqualTo(runId);
		assertThat(result.sessionId()).isNull();
	}

	@Test
	void repollExistingRun() throws Exception {
		final var eneoService = mock(EneoService.class);
		final var poller = mock(AppRunPoller.class);
		final var executor = new AppTargetExecutor(eneoService, poller);

		final var step = new Step().withId("S1").withName("S1").withTarget(new Step.Target(Step.Target.Type.APP, UUID.randomUUID()));
		final var flow = new Flow().withSteps(List.of(step));
		final var session = new Session("2281", flow, new StepExecutionFactory());
		final var exec = session.getStepExecution("S1");
		exec.setEneoRunId(UUID.randomUUID());

		when(poller.pollUntilComplete("2281", exec.getEneoRunId(), step.getName())).thenReturn("done2");

		final var stepRunContext = new StepRunContext("2281", session, exec, List.of(), List.of(), "", null, true);
		final var result = executor.execute(stepRunContext);

		assertThat(result.output()).isEqualTo("done2");
		assertThat(result.runId()).isEqualTo(exec.getEneoRunId());
		assertThat(result.sessionId()).isNull();
	}
}
