package se.sundsvall.ai.flow.integration.intric;

import generated.intric.ai.AskAssistant;
import generated.intric.ai.ModelId;
import generated.intric.ai.RunService;
import java.util.List;
import java.util.UUID;
import se.sundsvall.ai.flow.integration.intric.model.Response;

public final class IntricMapper {

	private IntricMapper() {
		// Prevent instantiation
	}

	public static RunService mapToRunService(final String input, final List<UUID> uploadedInputFilesInUse) {
		return new RunService()
			.input(input)
			.files(toModelIds(uploadedInputFilesInUse));
	}

	public static List<ModelId> toModelIds(final List<UUID> ids) {
		return ids.stream()
			.map(id -> new ModelId().id(id))
			.toList();
	}

	public static AskAssistant mapToAskAssistant(final String question, final List<UUID> uploadedInputFilesInUse) {
		return new AskAssistant()
			.question(question)
			.files(toModelIds(uploadedInputFilesInUse));
	}

	public static Response mapToResponse(final generated.intric.ai.AskResponse askResponse) {
		return new Response(askResponse.getSessionId(), askResponse.getAnswer());
	}

	public static Response mapToResponse(final generated.intric.ai.ServiceOutput serviceOutput) {
		return new Response(serviceOutput.getOutput());
	}

}
