package se.sundsvall.ai.flow.integration.eneo;

import generated.eneo.ai.AppRunPublic;
import generated.eneo.ai.AskAssistant;
import generated.eneo.ai.AskResponse;
import generated.eneo.ai.FilePublic;
import generated.eneo.ai.RunAppRequest;
import generated.eneo.ai.RunService;
import generated.eneo.ai.ServiceOutput;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static se.sundsvall.ai.flow.integration.eneo.EneoIntegration.CLIENT_ID;

@CircuitBreaker(name = CLIENT_ID)
public interface EneoClient {

	@PostMapping(value = "/services/{serviceId}/run/", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	ServiceOutput runService(@PathVariable UUID serviceId, @RequestBody RunService request);

	@PostMapping(value = "/assistants/{assistantId}/sessions/", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	AskResponse askAssistant(@PathVariable UUID assistantId, @RequestBody AskAssistant request);

	@PostMapping(value = "/assistants/{assistantId}/sessions/{sessionId}/", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	AskResponse askAssistantFollowup(@PathVariable UUID assistantId, @PathVariable UUID sessionId, @RequestBody AskAssistant request);

	@PostMapping(value = "/apps/{appId}/runs/", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	AppRunPublic runApp(@PathVariable UUID appId, @RequestBody RunAppRequest request);

	@GetMapping(value = "/app-runs/{runId}/", produces = APPLICATION_JSON_VALUE)
	AppRunPublic getAppRun(@PathVariable UUID runId);

	@PostMapping(value = "/files/", consumes = MULTIPART_FORM_DATA_VALUE, produces = APPLICATION_JSON_VALUE)
	ResponseEntity<FilePublic> uploadFile(@RequestPart(name = "upload_file") MultipartFile file);

	@DeleteMapping("/files/{fileId}/")
	ResponseEntity<Void> deleteFile(@PathVariable UUID fileId);
}
