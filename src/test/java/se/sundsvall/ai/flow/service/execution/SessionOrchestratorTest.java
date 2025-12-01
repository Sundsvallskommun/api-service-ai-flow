package se.sundsvall.ai.flow.service.execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.ai.flow.model.session.Session.State.ERROR;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.ai.flow.model.flowdefinition.Flow;
import se.sundsvall.ai.flow.model.flowdefinition.Step;
import se.sundsvall.ai.flow.model.session.Session;
import se.sundsvall.ai.flow.model.session.StepExecutionFactory;

@ExtendWith(MockitoExtension.class)
class SessionOrchestratorTest {

	@Mock
	private FileUploadManager fileUploadManager;

	@Mock
	private StepRunner stepRunner;

	@InjectMocks
	private SessionOrchestrator orchestrator;

	private Session newSessionWithTwoSteps() {
		final var step1 = new Step().withId("S1").withName("S1").withOrder(1);
		final var step2 = new Step().withId("S2").withName("S2").withOrder(2);
		final var flow = new Flow().withSteps(List.of(step1, step2));
		return new Session("2281", flow, new StepExecutionFactory());
	}

	@Test
	void runSession_happyPath() {
		final var session = newSessionWithTwoSteps();

		when(stepRunner.runStep(any())).thenReturn(new StepExecutionResult(true, "ok", UUID.randomUUID(), null, null));

		orchestrator.runSession("2281", session);

		verify(fileUploadManager).uploadMissing("2281", session);
		assertThat(session.getState()).isEqualTo(Session.State.FINISHED);
	}

	@Test
	void runSession_stepFailureSetsSessionError() {
		final var step1 = new Step().withId("S1").withName("S1").withOrder(1);
		final var flow = new Flow().withSteps(List.of(step1));
		final var session = new Session("2281", flow, new StepExecutionFactory());

		when(stepRunner.runStep(any())).thenReturn(StepExecutionResult.failure("boom"));

		orchestrator.runSession("2281", session);

		verify(fileUploadManager).uploadMissing("2281", session);
		assertThat(session.getState()).isEqualTo(ERROR);
	}
}
