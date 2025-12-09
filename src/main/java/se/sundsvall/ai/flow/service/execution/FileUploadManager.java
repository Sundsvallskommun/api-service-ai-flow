package se.sundsvall.ai.flow.service.execution;

import static java.util.function.Predicate.not;
import static se.sundsvall.dept44.util.LogUtils.sanitizeForLogging;

import java.util.Collection;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.sundsvall.ai.flow.integration.eneo.EneoService;
import se.sundsvall.ai.flow.model.session.Input;
import se.sundsvall.ai.flow.model.session.Session;

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
			.filter(not(Input::isUploadedToEneo))
			.forEach(input -> {
				LOG.info("Uploading file for input {}", sanitizeForLogging(input.getFile().getName()));
				final var eneoFileId = eneoService.uploadFile(municipalityId, input.getFile());
				LOG.info("Done uploading file for input {}", sanitizeForLogging(input.getFile().getName()));
				input.setEneoFileId(eneoFileId);
			});

		// Handle redirected output inputs by deleting old ones and uploading new ones
		final var inputsToRemoveFromSession = new HashMap<String, Input>();
		session.getRedirectedOutputInput().forEach((sourceStepId, inputs) -> {
			for (final var input : inputs) {
				if (input.isUploadedToEneo()) {
					LOG.info("Deleting previous redirected output file from step {} with id {}", sourceStepId, input.getEneoFileId());
					eneoService.deleteFile(municipalityId, input.getEneoFileId());
					inputsToRemoveFromSession.put(sourceStepId, input);
				} else {
					LOG.info("Uploading redirected output file from step {}", sourceStepId);
					final var eneoFileId = eneoService.uploadFile(municipalityId, input.getFile());
					input.setEneoFileId(eneoFileId);
					LOG.info("Uploaded redirected output file for step {} with id {}", sourceStepId, eneoFileId);
				}
			}
		});

		// Remove inputs that were replaced
		inputsToRemoveFromSession.forEach(session::removeRedirectedOutputInput);
	}
}
