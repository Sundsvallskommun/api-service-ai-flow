package se.sundsvall.ai.flow.model.session;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import se.sundsvall.ai.flow.model.flowdefinition.Flow;
import se.sundsvall.ai.flow.model.flowdefinition.Step;

class StepExecutionTest {

	@Test
	void getterAndSetter() {
		var session = new Session(new Flow());
		var step = new Step();
		var requiredStepExecutions = List.<StepExecution>of();

		var state = StepExecution.State.CREATED;
		var output = "output";
		var errorMessage = "errorMessage";
		var intricSessionId = UUID.randomUUID();

		var stepExecution = new StepExecution(session, step, requiredStepExecutions);
		assertThat(stepExecution.getState()).isEqualTo(state);

		state = StepExecution.State.ERROR;
		stepExecution.setState(state);
		stepExecution.setIntricSessionId(intricSessionId);
		stepExecution.setOutput(output);
		stepExecution.setErrorMessage(errorMessage);

		assertThat(stepExecution.getId()).isNotNull();
		assertThat(stepExecution.getSession().getId()).isEqualTo(session.getId());
		assertThat(stepExecution.getStep()).isEqualTo(step);
		assertThat(stepExecution.getRequiredStepExecutions()).isEqualTo(requiredStepExecutions);
		assertThat(stepExecution.getState()).isEqualTo(state);
		assertThat(stepExecution.getIntricSessionId()).isEqualTo(intricSessionId);
		assertThat(stepExecution.getOutput()).isEqualTo(output);
		assertThat(stepExecution.getErrorMessage()).isEqualTo(errorMessage);
		assertThat(stepExecution.isRunning()).isFalse();
	}

	@Test
	void setState() {
		var stepExecution = new StepExecution(null, null, null);

		assertThat(stepExecution.getStartedAt()).isNull();
		assertThat(stepExecution.getFinishedAt()).isNull();
		assertThat(stepExecution.getLastUpdatedAt()).isNotNull();

		stepExecution.setState(StepExecution.State.RUNNING);

		assertThat(stepExecution.getStartedAt()).isNotNull();
		assertThat(stepExecution.getFinishedAt()).isNull();
		assertThat(stepExecution.getLastUpdatedAt()).isNotNull();

		stepExecution.setState(StepExecution.State.DONE);

		assertThat(stepExecution.getStartedAt()).isNotNull();
		assertThat(stepExecution.getFinishedAt()).isNotNull();
		assertThat(stepExecution.getLastUpdatedAt()).isNotNull();
	}

	@Test
	void compareTo() {
		var stepExecution1 = new StepExecution(null, new Step().withOrder(5));
		var stepExecution2 = new StepExecution(null, new Step().withOrder(2));
		var stepExecution3 = new StepExecution(null, new Step().withOrder(7));

		assertThat(stepExecution1).isGreaterThan(stepExecution2).isLessThan(stepExecution3);
		assertThat(stepExecution2).isLessThan(stepExecution3);
	}
}
