package se.sundsvall.ai.flow.integration.intric;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import generated.intric.ai.AskAssistant;
import generated.intric.ai.FilePublic;
import generated.intric.ai.ModelId;
import generated.intric.ai.RunService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import se.sundsvall.ai.flow.integration.intric.model.Response;

@Component
public class IntricIntegration {

	public static final String CLIENT_ID = "intric";

	static final String INPUT_DELIMITER = "\n\n";

	private final IntricClient client;
	private final IntricClient intricClient;

	IntricIntegration(final IntricClient client, IntricClient intricClient) {
		this.client = client;
		this.intricClient = intricClient;
	}

	public Response runService(final UUID serviceId, final List<UUID> uploadedInputFilesInUse, final String uploadedInputFilesInUseInfo, final String question) {
		// Construct the actual input - no need to include the provided question if it's null or blank
		var actualInput = uploadedInputFilesInUseInfo;
		if (isNotBlank(question)) {
			actualInput += INPUT_DELIMITER + question;
		}

		var request = new RunService()
			.input(actualInput)
			.files(uploadedInputFilesInUse.stream().map(id -> new ModelId().id(id)).toList());
		var response = client.runService(serviceId, request);

		return new Response(response.getOutput());
	}

	public Response askAssistant(final UUID assistantId, final List<UUID> uploadedInputFilesInUse, final String uploadedInputFilesInUseInfo) {
		var request = new AskAssistant()
			.question(uploadedInputFilesInUseInfo)
			.files(uploadedInputFilesInUse.stream().map(id -> new ModelId().id(id)).toList());
		var response = intricClient.askAssistant(assistantId, request);

		return new Response(response.getSessionId(), response.getAnswer());
	}

	public Response askAssistantFollowup(final UUID assistantId, final UUID sessionId, final List<UUID> uploadedInputFilesInUse, final String uploadedInputFilesInUseInfo, final String question) {
		// Construct the actual question - no need to include the provided question if it's null or blank
		var actualQuestion = uploadedInputFilesInUseInfo;
		if (isNotBlank(question)) {
			actualQuestion += INPUT_DELIMITER + question;
		}

		var request = new AskAssistant()
			.question(actualQuestion)
			.files(uploadedInputFilesInUse.stream().map(id -> new ModelId().id(id)).toList());
		var response = intricClient.askAssistantFollowup(assistantId, sessionId, request);

		return new Response(response.getSessionId(), response.getAnswer());
	}

	public UUID uploadFile(final MultipartFile inputMultipartFile) {
		var response = intricClient.uploadFile(inputMultipartFile);

		return ofNullable(response.getBody())
			.map(FilePublic::getId)
			.orElseThrow(() -> Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "Unable to upload file to Intric"));
	}

	public void deleteFiles(final List<UUID> fileIds) {
		fileIds.forEach(this::deleteFile);
	}

	public void deleteFile(final UUID fileId) {
		intricClient.deleteFile(fileId);
	}
}
