package se.sundsvall.ai.flow.service.execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import se.sundsvall.ai.flow.model.flowdefinition.Step;
import se.sundsvall.ai.flow.model.session.Session;

class InputPreparationTest {

	@Test
	void prepare_delegatesToCollaborators() {
		final var redirectedOutputResolver = mock(RedirectedOutputResolver.class);
		final var fileUploadManager = mock(FileUploadManager.class);
		final var inputCollector = mock(InputCollector.class);

		final var sut = new InputPreparation(redirectedOutputResolver, fileUploadManager, inputCollector);

		final var municipalityId = "m";
		final var session = mock(Session.class);
		final var step = mock(Step.class);
		final var inputs = new InputCollector.Inputs(java.util.List.of(), "", java.util.List.of());

		when(inputCollector.resolve(session, step)).thenReturn(inputs);

		final var result = sut.prepare(municipalityId, session, step);

		// Verify interactions
		verify(redirectedOutputResolver).addRedirectedOutputsAsInputs(session, step);
		verify(fileUploadManager).uploadMissing(municipalityId, session);
		verify(inputCollector).resolve(session, step);

		assertThat(result).isSameAs(inputs);
	}
}
