package se.sundsvall.ai.flow.service.execution;

import org.springframework.stereotype.Component;
import se.sundsvall.ai.flow.model.flowdefinition.Step;
import se.sundsvall.ai.flow.model.session.Session;

@Component
public class InputPreparation {

	private final RedirectedOutputResolver redirectedOutputResolver;
	private final FileUploadManager fileUploadManager;
	private final InputCollector inputCollector;

	public InputPreparation(final RedirectedOutputResolver redirectedOutputResolver,
		final FileUploadManager fileUploadManager,
		final InputCollector inputCollector) {
		this.redirectedOutputResolver = redirectedOutputResolver;
		this.fileUploadManager = fileUploadManager;
		this.inputCollector = inputCollector;
	}

	public InputCollector.Inputs prepare(final String municipalityId, final Session session, final Step step) {
		redirectedOutputResolver.addRedirectedOutputsAsInputs(session, step);
		fileUploadManager.uploadMissing(municipalityId, session);
		return inputCollector.resolve(session, step);
	}
}
