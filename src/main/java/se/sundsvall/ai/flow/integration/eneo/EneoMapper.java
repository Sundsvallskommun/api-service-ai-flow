package se.sundsvall.ai.flow.integration.eneo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import generated.eneo.ai.AskAssistant;
import generated.eneo.ai.AskResponse;
import generated.eneo.ai.ModelId;
import generated.eneo.ai.RunService;
import generated.eneo.ai.ServiceOutput;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.problem.Problem;
import se.sundsvall.ai.flow.integration.eneo.model.Response;

import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;

public final class EneoMapper {

	private static final Logger LOG = LoggerFactory.getLogger(EneoMapper.class);
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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

	public static Response toResponse(final AskResponse askResponse) {
		return new Response(askResponse.getSessionId(), askResponse.getAnswer());
	}

	/**
	 * Converts a {@link ServiceOutput} to a {@link Response}.
	 *
	 * <p>
	 * The {@code ServiceOutput.output} field can contain different types depending on
	 * the Eneo service's configured output format:
	 * <ul>
	 * <li>{@code output_format = "string"} → {@link String}</li>
	 * <li>{@code output_format = "json"} → {@link java.util.Map Map&lt;String, Object&gt;}</li>
	 * <li>{@code output_format = "list"} → {@link java.util.List List&lt;String&gt;}</li>
	 * <li>{@code output_format = "boolean"} → {@link Boolean}</li>
	 * </ul>
	 *
	 * <p>
	 * All non-String types are serialized to their JSON string representation.
	 *
	 * @param  serviceOutput                        the service output from Eneo
	 * @return                                      a Response with the output as a string (or null if output was null)
	 * @throws org.zalando.problem.ThrowableProblem if serialization of non-String output fails
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

}
