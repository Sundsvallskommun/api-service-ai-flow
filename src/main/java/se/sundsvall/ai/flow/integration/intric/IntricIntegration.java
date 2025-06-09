package se.sundsvall.ai.flow.integration.intric;

import static org.zalando.problem.Status.BAD_GATEWAY;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import generated.intric.ai.AskAssistant;
import generated.intric.ai.AskResponse;
import generated.intric.ai.FilePublic;
import generated.intric.ai.RunService;
import generated.intric.ai.ServiceOutput;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.zalando.problem.Problem;

@Component
public class IntricIntegration {

	private static final Logger LOG = LoggerFactory.getLogger(IntricIntegration.class);

	private final IntricClient intricClient;
	private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

	IntricIntegration(final IntricClient intricClient) {
		this.intricClient = intricClient;
	}

	public ServiceOutput runService(final UUID serviceId, final RunService request) {
		try {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Running service with ID: {} and request: \n\n {}", serviceId, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(request));
			}
			var result = intricClient.runService(serviceId, request);
			if (LOG.isDebugEnabled()) {
				LOG.debug("Intric service with ID: {} returned output: \n\n {}", serviceId, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
			}
			return result;
		} catch (Exception e) {
			LOG.error("Error running service with ID: {}", serviceId, e);
			throw Problem.valueOf(BAD_GATEWAY, "Error running service with ID: %s".formatted(serviceId));
		}
	}

	public AskResponse askAssistant(final UUID assistantId, final AskAssistant request) {
		try {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Asking assistant with ID: {} and request: \n\n {}", assistantId, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(request));
			}
			var result = intricClient.askAssistant(assistantId, request);
			if (LOG.isDebugEnabled()) {
				LOG.debug("Intric assistant with ID: {} returned response: \n\n {}", assistantId, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
			}
			return result;
		} catch (Exception e) {
			LOG.error("Error asking assistant with ID: {}", assistantId, e);
			throw Problem.valueOf(BAD_GATEWAY, "Error asking assistant with ID: %s".formatted(assistantId));
		}
	}

	public AskResponse askAssistantFollowup(final UUID assistantId, final UUID sessionId, final AskAssistant request) {
		try {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Asking assistant followup with ID: {} and session ID: {} and request: \n\n {}", assistantId, sessionId, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(request));
			}
			var result = intricClient.askAssistantFollowup(assistantId, sessionId, request);
			if (LOG.isDebugEnabled()) {
				LOG.debug("Intric assistant followup with ID: {} and session ID: {} returned response: \n\n {}", assistantId, sessionId, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
			}
			return result;
		} catch (Exception e) {
			LOG.error("Error asking assistant with ID: {} and session ID: {}", assistantId, sessionId, e);
			throw Problem.valueOf(BAD_GATEWAY, "Error asking assistant with ID: %s and session ID: %s".formatted(assistantId, sessionId));
		}
	}

	public FilePublic uploadFile(final MultipartFile file) {
		try {
			LOG.debug("Uploading file: {}", file.getOriginalFilename());
			var result = intricClient.uploadFile(file);
			LOG.debug("File '{}' uploaded successfully", file.getOriginalFilename());
			return result.getBody();
		} catch (Exception e) {
			LOG.error("Error uploading file: {}", file.getOriginalFilename(), e);
			throw Problem.valueOf(BAD_GATEWAY, "Error uploading file: %s".formatted(file.getOriginalFilename()));
		}
	}

	public void deleteFile(final UUID fileId) {
		try {
			LOG.debug("Deleting file with ID: {}", fileId);
			intricClient.deleteFile(fileId);
			LOG.debug("File with ID: {} deleted successfully", fileId);
		} catch (Exception e) {
			LOG.error("Error deleting file with ID: {}", fileId, e);
			throw Problem.valueOf(BAD_GATEWAY, "Error deleting file with ID: %s".formatted(fileId));
		}
	}

}
