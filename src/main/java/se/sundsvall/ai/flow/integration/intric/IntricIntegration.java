package se.sundsvall.ai.flow.integration.intric;

import static org.zalando.problem.Status.BAD_GATEWAY;
import static se.sundsvall.dept44.util.LogUtils.sanitizeForLogging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import generated.intric.ai.AskAssistant;
import generated.intric.ai.AskResponse;
import generated.intric.ai.FilePublic;
import generated.intric.ai.RunService;
import generated.intric.ai.ServiceOutput;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.zalando.problem.Problem;
import se.sundsvall.ai.flow.integration.db.InstanceRepository;
import se.sundsvall.ai.flow.integration.intric.configuration.IntricClientFactory;

@Component
public class IntricIntegration {

	private static final Logger LOG = LoggerFactory.getLogger(IntricIntegration.class);

	private final IntricClientFactory intricClientFactory;
	private final InstanceRepository instanceRepository;
	private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

	/**
	 * The key is the municipality ID, and the value is the municipality specific configured IntricClient.
	 */
	public final Map<String, IntricClient> intricClients = new HashMap<>();

	IntricIntegration(final IntricClientFactory intricClientFactory,
		final InstanceRepository instanceRepository) {
		this.intricClientFactory = intricClientFactory;
		this.instanceRepository = instanceRepository;

		init();
	}

	void init() {
		instanceRepository.findAll().forEach(instanceEntity -> {
			var intricClient = intricClientFactory.createIntricClient(instanceEntity);
			intricClients.put(instanceEntity.getMunicipalityId(), intricClient);
		});
	}

	public ServiceOutput runService(final String municipalityId, final UUID serviceId, final RunService request) {
		try {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Running service with ID: {} and request: \n\n {}", serviceId, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(request));
			}

			var result = intricClients.get(municipalityId).runService(serviceId, request);
			if (LOG.isDebugEnabled()) {
				LOG.debug("Intric service with ID: {} returned output: \n\n {}", serviceId, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
			}
			return result;
		} catch (Exception e) {
			LOG.error("Error running service with ID: {}", serviceId, e);
			throw Problem.valueOf(BAD_GATEWAY, "Error running service with ID: %s".formatted(serviceId));
		}
	}

	public AskResponse askAssistant(final String municipalityId, final UUID assistantId, final AskAssistant request) {
		try {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Asking assistant with ID: {} and request: \n\n {}", assistantId, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(request));
			}
			var result = intricClients.get(municipalityId).askAssistant(assistantId, request);
			if (LOG.isDebugEnabled()) {
				LOG.debug("Intric assistant with ID: {} returned response: \n\n {}", assistantId, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
			}
			return result;
		} catch (Exception e) {
			LOG.error("Error asking assistant with ID: {}", assistantId, e);
			throw Problem.valueOf(BAD_GATEWAY, "Error asking assistant with ID: %s".formatted(assistantId));
		}
	}

	public AskResponse askAssistantFollowup(final String municipalityId, final UUID assistantId, final UUID sessionId, final AskAssistant request) {
		try {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Asking assistant followup with ID: {} and session ID: {} and request: \n\n {}", assistantId, sessionId, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(request));
			}
			var result = intricClients.get(municipalityId).askAssistantFollowup(assistantId, sessionId, request);
			if (LOG.isDebugEnabled()) {
				LOG.debug("Intric assistant followup with ID: {} and session ID: {} returned response: \n\n {}", assistantId, sessionId, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
			}
			return result;
		} catch (Exception e) {
			LOG.error("Error asking assistant with ID: {} and session ID: {}", assistantId, sessionId, e);
			throw Problem.valueOf(BAD_GATEWAY, "Error asking assistant with ID: %s and session ID: %s".formatted(assistantId, sessionId));
		}
	}

	public FilePublic uploadFile(final String municipalityId, final MultipartFile file) {
		try {
			LOG.debug("Uploading file: {}", sanitizeForLogging(file.getOriginalFilename()));
			var result = intricClients.get(municipalityId).uploadFile(file);
			LOG.debug("File '{}' uploaded successfully", sanitizeForLogging(file.getOriginalFilename()));
			return result.getBody();
		} catch (Exception e) {
			LOG.error("Error uploading file: {}", sanitizeForLogging(file.getOriginalFilename()), e);
			throw Problem.valueOf(BAD_GATEWAY, "Error uploading file: %s".formatted(file.getOriginalFilename()));
		}
	}

	public void deleteFile(final String municipalityId, final UUID fileId) {
		try {
			LOG.debug("Deleting file with ID: {}", fileId);
			intricClients.get(municipalityId).deleteFile(fileId);
			LOG.debug("File with ID: {} deleted successfully", fileId);
		} catch (Exception e) {
			LOG.error("Error deleting file with ID: {}", fileId, e);
			throw Problem.valueOf(BAD_GATEWAY, "Error deleting file with ID: %s".formatted(fileId));
		}
	}

}
