package se.sundsvall.ai.flow.service.execution;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import se.sundsvall.ai.flow.integration.eneo.EneoService;
import se.sundsvall.ai.flow.model.flowdefinition.Flow;
import se.sundsvall.ai.flow.model.flowdefinition.FlowInput;
import se.sundsvall.ai.flow.model.session.FileInputValue;
import se.sundsvall.ai.flow.model.session.Session;
import se.sundsvall.ai.flow.model.session.StepExecutionFactory;
import se.sundsvall.ai.flow.model.session.TextInputValue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FileUploadManagerTest {

	@Test
	void uploadsMissingAndReplacesRedirected() {
		final var eneo = mock(EneoService.class);
		final var manager = new FileUploadManager(eneo);

		final var flow = new Flow().withFlowInputs(List.of(new FlowInput().withId("A").withName("Doc A").withMultipleValued(true)));
		final var session = new Session("2281", flow, new StepExecutionFactory());

		// Regular input A not uploaded
		session.addInput("A", new FileInputValue("doc.txt", "hello".getBytes(), "text/plain"));

		// Redirected output: first an already uploaded one -> should be deleted and removed, then a not uploaded -> should be
		// uploaded
		session.addRedirectedOutputAsInput("S1", new TextInputValue("useAs", "old"));
		final var oldId = UUID.randomUUID();
		session.getRedirectedOutputInput().get("S1").getFirst().setEneoFileId(oldId);
		session.addRedirectedOutputAsInput("S1", new TextInputValue("useAs", "new"));

		// Stubs
		when(eneo.uploadFile(eq("2281"), any())).thenReturn(UUID.randomUUID());

		// Act
		manager.uploadMissing("2281", session);

		// Assert uploads: 2 uploads expected (regular A + new redirected)
		verify(eneo, times(2)).uploadFile(eq("2281"), any());
		// Assert deletion of old redirected
		verify(eneo).deleteFile("2281", oldId);

		// Ensure old redirected removed from the session list
		assertThat(session.getRedirectedOutputInput().get("S1")).hasSize(1);
	}
}
