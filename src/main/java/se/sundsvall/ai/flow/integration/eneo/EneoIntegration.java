package se.sundsvall.ai.flow.integration.eneo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import generated.eneo.ai.AskAssistant;
import generated.eneo.ai.AskResponse;
import generated.eneo.ai.FilePublic;
import generated.eneo.ai.RunService;
import generated.eneo.ai.ServiceOutput;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.zalando.problem.Problem;

import static java.util.Optional.ofNullable;
import static org.zalando.problem.Status.BAD_GATEWAY;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;
import static se.sundsvall.dept44.util.LogUtils.sanitizeForLogging;

@Component
public class EneoIntegration {

	private static final Logger LOG = LoggerFactory.getLogger(EneoIntegration.class);

	static final String CLIENT_ID = "eneo";

	private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

	/**
	 * The key is the municipality ID, and the value is the municipality specific configured EneoClient.
	 */
	private final Map<String, EneoClient> eneoClients;

	public EneoIntegration(final Map<String, EneoClient> eneoClients) {
		this.eneoClients = eneoClients;
	}

	private EneoClient getEneoClient(final String municipalityId) {
		return ofNullable(eneoClients.get(municipalityId)).orElseThrow(() -> Problem.valueOf(INTERNAL_SERVER_ERROR, "Eneo client not configured for municipality ID: %s".formatted(municipalityId)));
	}

	public ServiceOutput runService(final String municipalityId, final UUID serviceId, final RunService request) {
		try {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Running service with ID: {} and request: \n\n {}", serviceId, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(request));
			}

			final var result = getEneoClient(municipalityId).runService(serviceId, request);
			if (LOG.isDebugEnabled()) {
				LOG.debug("Eneo service with ID: {} returned output: \n\n {}", serviceId, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
			}
			return result;
		} catch (final Exception e) {
			LOG.error("Error running service with ID: {}", serviceId, e);
			throw Problem.valueOf(BAD_GATEWAY, "Error running service with ID: %s".formatted(serviceId));
		}
	}

	public AskResponse askAssistant(final String municipalityId, final UUID assistantId, final AskAssistant request) {
		try {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Asking assistant with ID: {} and request: \n\n {}", assistantId, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(request));
			}
			final var result = getEneoClient(municipalityId).askAssistant(assistantId, request);
			if (LOG.isDebugEnabled()) {
				LOG.debug("Eneo assistant with ID: {} returned response: \n\n {}", assistantId, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
			}
			return result;
		} catch (final Exception e) {
			LOG.error("Error asking assistant with ID: {}", assistantId, e);
			throw Problem.valueOf(BAD_GATEWAY, "Error asking assistant with ID: %s".formatted(assistantId));
		}
	}

	public AskResponse askAssistantFollowup(final String municipalityId, final UUID assistantId, final UUID sessionId, final AskAssistant request) {
		try {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Asking assistant followup with ID: {} and session ID: {} and request: \n\n {}", assistantId, sessionId, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(request));
			}
			final var result = getEneoClient(municipalityId).askAssistantFollowup(assistantId, sessionId, request);
			if (LOG.isDebugEnabled()) {
				LOG.debug("Eneo assistant followup with ID: {} and session ID: {} returned response: \n\n {}", assistantId, sessionId, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
			}
			return result;
		} catch (final Exception e) {
			LOG.error("Error asking assistant with ID: {} and session ID: {}", assistantId, sessionId, e);
			throw Problem.valueOf(BAD_GATEWAY, "Error asking assistant with ID: %s and session ID: %s".formatted(assistantId, sessionId));
		}
	}

	public FilePublic uploadFile(final String municipalityId, final MultipartFile file) {
		try {
			LOG.debug("Uploading file: {}", sanitizeForLogging(file.getOriginalFilename()));
			final var result = getEneoClient(municipalityId).uploadFile(file);
			LOG.debug("File '{}' uploaded successfully", sanitizeForLogging(file.getOriginalFilename()));
			return result.getBody();
		} catch (final Exception e) {
			LOG.error("Error uploading file: {}", sanitizeForLogging(file.getOriginalFilename()), e);
			throw Problem.valueOf(BAD_GATEWAY, "Error uploading file: %s".formatted(file.getOriginalFilename()));
		}
	}

	public void deleteFile(final String municipalityId, final UUID fileId) {
		try {
			LOG.debug("Deleting file with ID: {}", fileId);
			getEneoClient(municipalityId).deleteFile(fileId);
			LOG.debug("File with ID: {} deleted successfully", fileId);
		} catch (final Exception e) {
			LOG.error("Error deleting file with ID: {}", fileId, e);
			throw Problem.valueOf(BAD_GATEWAY, "Error deleting file with ID: %s".formatted(fileId));
		}
	}
}
