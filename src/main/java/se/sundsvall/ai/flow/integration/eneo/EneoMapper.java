package se.sundsvall.ai.flow.integration.eneo;

import generated.eneo.ai.AskAssistant;
import generated.eneo.ai.ModelId;
import generated.eneo.ai.RunService;
import java.util.List;
import java.util.UUID;
import se.sundsvall.ai.flow.integration.eneo.model.Response;

public final class EneoMapper {

	private EneoMapper() {
		// Prevent instantiation
	}

	public static RunService toRunService(final String input, final List<UUID> uploadedInputFilesInUse) {
		return new RunService()
			.input(input)
			.files(toModelIds(uploadedInputFilesInUse));
	}

	public static List<ModelId> toModelIds(final List<UUID> ids) {
		return ids.stream()
			.map(id -> new ModelId().id(id))
			.toList();
	}

	public static AskAssistant toAskAssistant(final String question, final List<UUID> uploadedInputFilesInUse) {
		return new AskAssistant()
			.question(question)
			.files(toModelIds(uploadedInputFilesInUse));
	}

	public static Response toResponse(final generated.eneo.ai.AskResponse askResponse) {
		return new Response(askResponse.getSessionId(), askResponse.getAnswer());
	}

	public static Response toResponse(final generated.eneo.ai.ServiceOutput serviceOutput) {
		return new Response(serviceOutput.getOutput().toString());
	}

}
