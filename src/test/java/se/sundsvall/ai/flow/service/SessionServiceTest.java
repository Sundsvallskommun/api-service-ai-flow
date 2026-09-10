package se.sundsvall.ai.flow.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.ai.flow.integration.eneo.EneoService;
import se.sundsvall.ai.flow.integration.templating.TemplatingIntegration;
import se.sundsvall.ai.flow.model.session.Session;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.ThrowableProblem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static se.sundsvall.ai.flow.TestDataFactory.MUNICIPALITY_ID;
import static se.sundsvall.ai.flow.TestDataFactory.createFlow;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

	@Mock
	private Executor executorMock;

	@Mock
	private EneoService eneoServiceMock;

	@Mock
	private TemplatingIntegration templatingIntegrationMock;

	@InjectMocks
	private SessionService sessionService;

	@Test
	void deleteSessionRemovesConversationsAndAppRunsBeforeFilesAndForgetsTheSession() {
		final var session = createSessionWithUploadedFiles();
		final var fileIds = uploadedFileIds(session);
		final var retiredFileId = UUID.randomUUID();
		session.markFileForDeletion(retiredFileId);
		final var conversationId = UUID.randomUUID();
		final var runId = UUID.randomUUID();
		session.getStepExecutions().get("step1").setEneoSessionId(conversationId);
		session.getStepExecutions().get("step2").setEneoRunId(runId);

		sessionService.deleteSession(MUNICIPALITY_ID, session.getId());

		final var inOrder = inOrder(eneoServiceMock);
		inOrder.verify(eneoServiceMock).deleteConversation(MUNICIPALITY_ID, conversationId);
		inOrder.verify(eneoServiceMock).deleteAppRun(MUNICIPALITY_ID, runId);
		inOrder.verify(eneoServiceMock, times(fileIds.size() + 1)).deleteFile(eq(MUNICIPALITY_ID), any(UUID.class));
		fileIds.forEach(fileId -> verify(eneoServiceMock).deleteFile(MUNICIPALITY_ID, fileId));
		verify(eneoServiceMock).deleteFile(MUNICIPALITY_ID, retiredFileId);
		verifyNoMoreInteractions(eneoServiceMock);
		assertThat(sessionService.getAllSessions()).isEmpty();
	}

	@Test
	void deleteSessionKeepsGoingWhenEneoRefusesADelete() {
		final var session = createSessionWithUploadedFiles();
		final var fileIds = uploadedFileIds(session);
		final var conversationId = UUID.randomUUID();
		session.getStepExecutions().get("step1").setEneoSessionId(conversationId);

		doThrow(Problem.valueOf(BAD_GATEWAY, "Error deleting conversation")).when(eneoServiceMock).deleteConversation(MUNICIPALITY_ID, conversationId);
		doThrow(Problem.valueOf(CONFLICT, "still in use")).when(eneoServiceMock).deleteFile(MUNICIPALITY_ID, fileIds.getFirst());

		sessionService.deleteSession(MUNICIPALITY_ID, session.getId());

		verify(eneoServiceMock).deleteConversation(MUNICIPALITY_ID, conversationId);
		fileIds.forEach(fileId -> verify(eneoServiceMock).deleteFile(MUNICIPALITY_ID, fileId));
		verifyNoMoreInteractions(eneoServiceMock);
		assertThat(sessionService.getAllSessions()).isEmpty();
	}

	@Test
	void deleteSessionWithoutUploadedFilesTouchesNothingInEneo() {
		final var session = sessionService.createSession(MUNICIPALITY_ID, createFlow());

		sessionService.deleteSession(MUNICIPALITY_ID, session.getId());

		verifyNoInteractions(eneoServiceMock);
		assertThat(sessionService.getAllSessions()).isEmpty();
	}

	@Test
	void deleteSessionWhenSessionDoesNotExist() {
		final var sessionId = UUID.randomUUID();

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> sessionService.deleteSession(MUNICIPALITY_ID, sessionId))
			.satisfies(problem -> assertThat(problem.getStatus()).isEqualTo(NOT_FOUND))
			.withMessageContaining("No session exists with id " + sessionId);

		verifyNoInteractions(eneoServiceMock);
	}

	private Session createSessionWithUploadedFiles() {
		final var flow = createFlow();
		final var session = sessionService.createSession(MUNICIPALITY_ID, flow);
		flow.getFlowInputs().forEach(flowInput -> session.addSimpleInput(flowInput.getId(), "value"));
		session.getInput().values().stream()
			.flatMap(Collection::stream)
			.forEach(input -> input.setIntricFileId(UUID.randomUUID()));
		return session;
	}

	private static List<UUID> uploadedFileIds(final Session session) {
		final var fileIds = new ArrayList<UUID>();
		session.getInput().values().stream()
			.flatMap(Collection::stream)
			.forEach(input -> fileIds.add(input.getIntricFileId()));
		return fileIds;
	}
}
