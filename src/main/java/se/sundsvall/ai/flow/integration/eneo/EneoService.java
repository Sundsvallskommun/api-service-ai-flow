package se.sundsvall.ai.flow.integration.eneo;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static se.sundsvall.ai.flow.integration.eneo.EneoMapper.toAskAssistant;
import static se.sundsvall.ai.flow.integration.eneo.EneoMapper.toResponse;
import static se.sundsvall.ai.flow.integration.eneo.EneoMapper.toRunAppRequest;
import static se.sundsvall.ai.flow.integration.eneo.EneoMapper.toRunService;

import generated.eneo.ai.FilePublic;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import se.sundsvall.ai.flow.integration.eneo.model.Response;

@Service
public class EneoService {

	static final String INPUT_DELIMITER = "\n\n";

	private final EneoIntegration eneoIntegration;

	public EneoService(final EneoIntegration eneoIntegration) {
		this.eneoIntegration = eneoIntegration;
	}

	public Response runService(final String municipalityId, final UUID serviceId, final List<UUID> uploadedInputFilesInUse, final String uploadedInputFilesInUseInfo, final String question) {
		// Construct the actual input - no need to include the provided question if it's null or blank
		var actualInput = uploadedInputFilesInUseInfo;
		if (isNotBlank(question)) {
			actualInput += INPUT_DELIMITER + question;
		}

		var runServiceRequest = toRunService(actualInput, uploadedInputFilesInUse);
		var response = eneoIntegration.runService(municipalityId, serviceId, runServiceRequest);

		return toResponse(response);
	}

	public Response askAssistant(final String municipalityId, final UUID assistantId, final List<UUID> uploadedInputFilesInUse, final String uploadedInputFilesInUseInfo) {
		var askAssistantRequest = toAskAssistant(uploadedInputFilesInUseInfo, uploadedInputFilesInUse);
		var response = eneoIntegration.askAssistant(municipalityId, assistantId, askAssistantRequest);

		return EneoMapper.toResponse(response);
	}

	public Response askAssistantFollowup(final String municipalityId, final UUID assistantId, final UUID sessionId, final List<UUID> uploadedInputFilesInUse, final String uploadedInputFilesInUseInfo, final String question) {
		// Construct the actual question - no need to include the provided question if it's null or blank
		var actualQuestion = uploadedInputFilesInUseInfo;
		if (isNotBlank(question)) {
			actualQuestion += INPUT_DELIMITER + question;
		}

		var request = toAskAssistant(actualQuestion, uploadedInputFilesInUse);
		var response = eneoIntegration.askAssistantFollowup(municipalityId, assistantId, sessionId, request);

		return EneoMapper.toResponse(response);
	}

	public UUID uploadFile(final String municipalityId, final MultipartFile inputMultipartFile) {
		var filePublic = eneoIntegration.uploadFile(municipalityId, inputMultipartFile);

		return ofNullable(filePublic)
			.map(FilePublic::getId)
			.orElseThrow(() -> Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "Unable to upload file to Eneo"));
	}

	public void deleteFiles(final String municipalityId, final List<UUID> fileIds) {
		fileIds.forEach(uuid -> deleteFile(municipalityId, uuid));
	}

	public void deleteFile(final String municipalityId, final UUID fileId) {
		eneoIntegration.deleteFile(municipalityId, fileId);
	}

	public Response runApp(final String municipalityId, final UUID appId, final List<UUID> uploadedInputFilesInUse) {
		// APP endpoints only works with files and doesn't accept text input even though the RunAppRequest model contains an
		// input field.
		var runAppRequest = toRunAppRequest(uploadedInputFilesInUse);
		var response = eneoIntegration.runApp(municipalityId, appId, runAppRequest);

		return toResponse(response);
	}

	public Response getAppRun(final String municipalityId, final UUID runId) {
		var response = eneoIntegration.getAppRun(municipalityId, runId);

		return toResponse(response);
	}
}
