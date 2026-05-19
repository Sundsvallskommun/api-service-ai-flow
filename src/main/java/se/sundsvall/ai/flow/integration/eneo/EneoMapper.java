package se.sundsvall.ai.flow.integration.eneo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import generated.eneo.ai.AppRunPublic;
import generated.eneo.ai.AskAssistant;
import generated.eneo.ai.AskResponse;
import generated.eneo.ai.ModelId;
import generated.eneo.ai.RunAppRequest;
import generated.eneo.ai.RunService;
import generated.eneo.ai.ServiceOutput;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sundsvall.ai.flow.integration.eneo.model.Response;
import se.sundsvall.dept44.problem.Problem;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

public final class EneoMapper {

	private static final Logger LOG = LoggerFactory.getLogger(EneoMapper.class);
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private EneoMapper() {
		// Prevent instantiation
	}

	public static RunService toRunService(final String input, final List<UUID> uploadedInputFilesInUse) {
		return new RunService()
			.input(input)
			.files(uploadedInputFilesInUse);
	}

	public static AskAssistant toAskAssistant(final String question, final List<UUID> uploadedInputFilesInUse) {
		return new AskAssistant()
			.question(question)
			.files(uploadedInputFilesInUse);
	}

	public static RunAppRequest toRunAppRequest(final List<UUID> uploadedInputFilesInUse) {
		return new RunAppRequest()
			.files(uploadedInputFilesInUse.stream()
				.map(id -> new ModelId().id(id))
				.toList());
	}

	public static Response toResponse(final AskResponse askResponse) {
		return new Response(askResponse.getSessionId(), askResponse.getAnswer());
	}

	/**
	 * The {@code ServiceOutput.output} field is a polymorphic value whose runtime type depends on
	 * the Eneo service's configured output format (string, json, list, boolean). Non-String values
	 * are serialized to their JSON representation so downstream consumers always see a String.
	 */
	public static Response toResponse(final ServiceOutput serviceOutput) {
		final var output = serviceOutput.getOutput();
		final var outputAsString = switch (output) {
			case String s -> s;
			case null -> null;
			default -> {
				try {
					yield OBJECT_MAPPER.writeValueAsString(output);
				} catch (final JsonProcessingException e) {
					LOG.error("Failed to serialize ServiceOutput of type {} to JSON string. Value: {}", output.getClass().getName(), output, e);
					throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Failed to serialize ServiceOutput to JSON string");
				}
			}
		};
		return new Response(outputAsString);
	}

	// Response for APP run
	public static Response toResponse(final AppRunPublic appRunPublic) {
		return new Response(appRunPublic.getId(), appRunPublic.getOutput(), appRunPublic.getStatus());
	}
}
