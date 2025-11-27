package se.sundsvall.ai.flow.service.execution;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import se.sundsvall.ai.flow.model.flowdefinition.Flow;
import se.sundsvall.ai.flow.model.flowdefinition.Step;
import se.sundsvall.ai.flow.model.session.Session;
import se.sundsvall.ai.flow.model.session.StepExecution;
import se.sundsvall.ai.flow.model.session.StepExecutionFactory;

class StepStateUpdaterTest {

	@Test
	void transitionsAreApplied() {
		final var flow = new Flow().withSteps(java.util.List.of(new Step().withId("S1")));
		final var session = new Session("2281", flow, new StepExecutionFactory());
		final var exec = session.getStepExecution("S1");

		final var updater = new StepStateUpdater();
		updater.markRunning(exec);
		assertThat(exec.getState()).isEqualTo(StepExecution.State.RUNNING);
		assertThat(exec.getStartedAt()).isNotNull();

		final var runId = UUID.randomUUID();
		final var sessionId = UUID.randomUUID();
		updater.markFinished(exec, "out", runId, sessionId);
		assertThat(exec.getState()).isEqualTo(StepExecution.State.DONE);
		assertThat(exec.getOutput()).isEqualTo("out");
		assertThat(exec.getIntricRunId()).isEqualTo(runId);
		assertThat(exec.getIntricSessionId()).isEqualTo(sessionId);

		updater.markFailed(exec, "err");
		assertThat(exec.getState()).isEqualTo(StepExecution.State.ERROR);
		assertThat(exec.getErrorMessage()).isEqualTo("err");
	}
}
