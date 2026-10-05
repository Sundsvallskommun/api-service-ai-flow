package se.sundsvall.ai.flow.service.execution;

import java.util.Collection;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.sundsvall.ai.flow.integration.eneo.EneoService;
import se.sundsvall.ai.flow.model.session.Input;
import se.sundsvall.ai.flow.model.session.Session;

import static java.util.function.Predicate.not;
import static se.sundsvall.dept44.util.LogUtils.sanitizeForLogging;

/**
 * Ensures session inputs are uploaded to Eneo, handling redirected outputs as well.
 */
@Component
public class FileUploadManager {
	private static final Logger LOG = LoggerFactory.getLogger(FileUploadManager.class);

	private final EneoService eneoService;

	public FileUploadManager(final EneoService eneoService) {
		this.eneoService = eneoService;
	}

	public void uploadMissing(final String municipalityId, final Session session) {
		// Upload any missing regular inputs
		session.getInput().values().stream()
			.flatMap(Collection::stream)
			.filter(not(Input::isUploadedToIntric))
			.forEach(input -> {
				LOG.info("Uploading file for input {}", sanitizeForLogging(input.getFile().getName()));
				final var intricFileId = eneoService.uploadFile(municipalityId, input.getFile());
				LOG.info("Done uploading file for input {}", sanitizeForLogging(input.getFile().getName()));
				input.setIntricFileId(intricFileId);
			});

		// Handle redirected output inputs by retiring old ones and uploading new ones. The old file is not deleted here:
		// it is usually still attached to the consuming step's Eneo conversation, which would reject the delete with 409.
		// It is deleted together with the session instead.
		final var inputsToRemoveFromSession = new HashMap<String, Input>();
		session.getRedirectedOutputInput().forEach((sourceStepId, inputs) -> {
			for (final var input : inputs) {
				if (input.isUploadedToIntric()) {
					LOG.info("Retiring previous redirected output file from step {} with id {}", sourceStepId, input.getIntricFileId());
					session.markFileForDeletion(input.getIntricFileId());
					inputsToRemoveFromSession.put(sourceStepId, input);
				} else {
					LOG.info("Uploading redirected output file from step {}", sourceStepId);
					final var intricFileId = eneoService.uploadFile(municipalityId, input.getFile());
					input.setIntricFileId(intricFileId);
					LOG.info("Uploaded redirected output file for step {} with id {}", sourceStepId, intricFileId);
				}
			}
		});

		// Remove inputs that were replaced
		inputsToRemoveFromSession.forEach(session::removeRedirectedOutputInput);
	}
}
