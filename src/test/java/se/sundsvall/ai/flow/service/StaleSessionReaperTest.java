package se.sundsvall.ai.flow.service;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.ai.flow.model.session.Session;

@ExtendWith(MockitoExtension.class)
class StaleSessionReaperTest {

	@Mock
	private SessionService mockSessionService;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private Session mockSession1;
	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private Session mockSession2;

	@InjectMocks
	private StaleSessionReaper staleSessionReaper;

	@Test
	void runWhenNoSessionsExist() {
		when(mockSessionService.getAllSessions()).thenReturn(emptyList());

		staleSessionReaper.run();

		verify(mockSessionService).getAllSessions();
		verifyNoMoreInteractions(mockSessionService);
	}

	@Test
	void run() {
		var sessionId1 = UUID.randomUUID();
		var sessionId2 = UUID.randomUUID();

		when(mockSession1.getId()).thenReturn(sessionId1);
		when(mockSession1.getFlow().getTtlInMinutes()).thenReturn(30);
		when(mockSession1.getLastUpdatedAt()).thenReturn(LocalDateTime.now().minusMinutes(45));

		when(mockSession2.getId()).thenReturn(sessionId2);
		when(mockSession2.getFlow().getTtlInMinutes()).thenReturn(30);
		when(mockSession2.getLastUpdatedAt()).thenReturn(LocalDateTime.now().minusMinutes(15));

		when(mockSessionService.getAllSessions()).thenReturn(List.of(mockSession1, mockSession2));

		staleSessionReaper.run();

		verify(mockSessionService).getAllSessions();
		verify(mockSessionService).deleteSession(sessionId1);
		verifyNoMoreInteractions(mockSessionService);
	}
}
