package se.sundsvall.ai.flow.service.execution;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import se.sundsvall.ai.flow.model.flowdefinition.Flow;
import se.sundsvall.ai.flow.model.flowdefinition.Step;
import se.sundsvall.ai.flow.model.session.Session;
import se.sundsvall.ai.flow.model.session.StepExecution;
import se.sundsvall.ai.flow.model.session.StepExecutionFactory;

import static org.assertj.core.api.Assertions.assertThat;

class StepStateUpdaterTest {

	@Test
	void transitionsAreApplied() {
		final var flow = new Flow().withSteps(List.of(new Step().withId("S1")));
		final var session = new Session("2281", flow, new StepExecutionFactory());
		final var stepExecution = session.getStepExecution("S1");

		final var updater = new StepStateUpdater();
		updater.markRunning(stepExecution);
		assertThat(stepExecution.getState()).isEqualTo(StepExecution.State.RUNNING);
		assertThat(stepExecution.getStartedAt()).isNotNull();

		final var runId = UUID.randomUUID();
		final var sessionId = UUID.randomUUID();
		updater.markFinished(stepExecution, "out", runId, sessionId);
		assertThat(stepExecution.getState()).isEqualTo(StepExecution.State.DONE);
		assertThat(stepExecution.getOutput()).isEqualTo("out");
		assertThat(stepExecution.getEneoRunId()).isEqualTo(runId);
		assertThat(stepExecution.getEneoSessionId()).isEqualTo(sessionId);

		updater.markFailed(stepExecution, "err");
		assertThat(stepExecution.getState()).isEqualTo(StepExecution.State.ERROR);
		assertThat(stepExecution.getErrorMessage()).isEqualTo("err");
	}
}
