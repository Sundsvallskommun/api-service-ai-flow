package se.sundsvall.ai.flow.api;

import static java.util.Optional.ofNullable;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.util.UriComponentsBuilder.fromPath;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.ai.flow.api.model.ChatRequest;
import se.sundsvall.ai.flow.api.model.CreateSessionRequest;
import se.sundsvall.ai.flow.api.model.Output;
import se.sundsvall.ai.flow.api.model.RenderRequest;
import se.sundsvall.ai.flow.api.model.SimpleInput;
import se.sundsvall.ai.flow.model.session.Session;
import se.sundsvall.ai.flow.model.session.StepExecution;
import se.sundsvall.ai.flow.service.FlowService;
import se.sundsvall.ai.flow.service.SessionService;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;

@RestController
@RequestMapping("/{municipalityId}/session")
@Tag(name = "Sessions", description = "Session resources")
@ApiResponse(
	responseCode = "400",
	description = "Bad Request",
	content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
		Problem.class, ConstraintViolationProblem.class
	})))
@ApiResponse(
	responseCode = "500",
	description = "Internal Server Error",
	content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
class SessionResource {

	private final FlowService flowService;
	private final SessionService sessionService;

	SessionResource(final FlowService flowService, final SessionService sessionService) {
		this.flowService = flowService;
		this.sessionService = sessionService;
	}

	@Operation(
		summary = "Get a session",
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "Ok",
				useReturnTypeSchema = true),
			@ApiResponse(
				responseCode = "404",
				description = "Not Found",
				content = @Content(schema = @Schema(implementation = Problem.class)))
		})
	@GetMapping(value = "/{sessionId}", produces = APPLICATION_JSON_VALUE)
	ResponseEntity<Session> getSession(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "sessionId", description = "Session id") @PathVariable("sessionId") final UUID sessionId) {
		return ok(sessionService.getSession(sessionId));
	}

	@Operation(
		summary = "Create a session for a given flow",
		responses = {
			@ApiResponse(
				responseCode = "201",
				description = "Ok",
				useReturnTypeSchema = true),
			@ApiResponse(
				responseCode = "404",
				description = "Not Found",
				content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
		})
	@PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	ResponseEntity<Session> createSession(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Valid @RequestBody final CreateSessionRequest request) {
		final var flow = ofNullable(request.version())
			.map(version -> flowService.getFlowVersion(request.flowId(), version))
			.orElseGet(() -> flowService.getLatestFlowVersion(request.flowId()));
		final var session = sessionService.createSession(municipalityId, flow);

		return created(fromPath("/session/{sessionId}").buildAndExpand(session.getId()).toUri())
			.body(session);
	}

	@Operation(
		summary = "Run (all steps in) a session",
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "Ok",
				useReturnTypeSchema = true),
			@ApiResponse(
				responseCode = "404",
				description = "Not Found",
				content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
		})
	@PostMapping(value = "/{sessionId}", produces = APPLICATION_JSON_VALUE)
	ResponseEntity<Void> runSession(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "sessionId", description = "Session id") @PathVariable("sessionId") final UUID sessionId) {
		sessionService.executeSession(municipalityId, sessionId);

		return ResponseEntity.ok().build();
	}

	@Operation(
		summary = "Delete a session",
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "Ok",
				useReturnTypeSchema = true),
			@ApiResponse(
				responseCode = "404",
				description = "Not Found",
				content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
		})
	@DeleteMapping(value = "/{sessionId}", produces = APPLICATION_JSON_VALUE)
	ResponseEntity<Void> deleteSession(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "sessionId", description = "Session id") @PathVariable("sessionId") final UUID sessionId) {
		sessionService.deleteSession(municipalityId, sessionId);
		return ok().build();
	}

	@Operation(
		summary = "Get a step (execution) for a session",
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "Ok",
				useReturnTypeSchema = true),
			@ApiResponse(
				responseCode = "404",
				description = "Not Found",
				content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
		})
	@GetMapping(value = "/{sessionId}/step/{stepId}", produces = APPLICATION_JSON_VALUE)
	ResponseEntity<StepExecution> getStep(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "sessionId", description = "Session id") @PathVariable("sessionId") final UUID sessionId,
		@Parameter(name = "stepId", description = "Step id") @PathVariable("stepId") String stepId) {
		return ok(sessionService.getStepExecution(sessionId, stepId));
	}

	@Operation(
		summary = "Run/re-run a step (execution) for a session",
		responses = {
			@ApiResponse(
				responseCode = "201",
				description = "Ok",
				useReturnTypeSchema = true),
			@ApiResponse(
				responseCode = "404",
				description = "Not Found",
				content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
		})
	@PostMapping(value = "/{sessionId}/step/{stepId}", consumes = APPLICATION_JSON_VALUE, produces = ALL_VALUE)
	ResponseEntity<Void> runStep(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "sessionId", description = "Session id") @PathVariable("sessionId") final UUID sessionId,
		@Parameter(name = "stepId", description = "Step id") @PathVariable("stepId") String stepId,
		@Valid @RequestBody final ChatRequest request) {
		sessionService.executeStep(municipalityId, sessionId, stepId, request.input(), request.runRequiredSteps());

		return created(fromPath("/{municipalityId}/session/{sessionId}/step/{stepId}")
			.buildAndExpand(municipalityId, sessionId, stepId).toUri())
			.build();
	}

	@Operation(
		summary = " Add simple (text) input to a session",
		description = "If the input is single-valued - the input value is replaced entirely",
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "Ok",
				useReturnTypeSchema = true),
			@ApiResponse(
				responseCode = "404",
				description = "Not Found",
				content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
		})
	@PostMapping(value = "/{sessionId}/input/{inputId}/simple", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	ResponseEntity<Session> addSimpleInputToSession(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "sessionId", description = "Session id") @PathVariable("sessionId") final UUID sessionId,
		@Parameter(name = "inputId", description = "Input id") @PathVariable("inputId") final String inputId,
		@Valid @RequestBody final SimpleInput input) {
		return ResponseEntity.ok(sessionService.addInput(sessionId, inputId, input.value()));
	}

	@Operation(
		summary = " Add binary (file) input to a session",
		description = "If the input is single-valued - the input value is replaced entirely",
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "Ok",
				useReturnTypeSchema = true),
			@ApiResponse(
				responseCode = "404",
				description = "Not Found",
				content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
		})
	@PostMapping(value = "/{sessionId}/input/{inputId}/file", consumes = MULTIPART_FORM_DATA_VALUE, produces = APPLICATION_JSON_VALUE)
	ResponseEntity<Session> addFileInputToSession(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "sessionId", description = "Session id") @PathVariable("sessionId") final UUID sessionId,
		@Parameter(name = "inputId", description = "Input id") @PathVariable("inputId") final String inputId,
		@RequestPart("file") final MultipartFile inputMultipartFile) {
		return ResponseEntity.ok(sessionService.addInput(sessionId, inputId, inputMultipartFile));
	}

	@Operation(
		summary = "Clear an input in a session",
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "Ok",
				useReturnTypeSchema = true),
			@ApiResponse(
				responseCode = "404",
				description = "Not Found",
				content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
		})
	@DeleteMapping(value = "/{sessionId}/input/{inputId}", produces = APPLICATION_JSON_VALUE)
	ResponseEntity<Session> clearInputInSession(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "sessionId", description = "Session id") @PathVariable("sessionId") final UUID sessionId,
		@Parameter(name = "inputId", description = "Input id") @PathVariable("inputId") final String inputId) {
		return ok(sessionService.clearInput(sessionId, inputId));
	}

	@Operation(
		summary = "Generate session output",
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "Ok",
				useReturnTypeSchema = true),
			@ApiResponse(
				responseCode = "404",
				description = "Not Found",
				content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
		})
	@PostMapping(value = "/{sessionId}/generate", consumes = APPLICATION_JSON_VALUE, produces = ALL_VALUE)
	ResponseEntity<Output> generateSessionOutput(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "sessionId", description = "Session id") @PathVariable("sessionId") final UUID sessionId,
		@RequestBody(required = false) @Valid final RenderRequest renderRequest) {
		final var session = sessionService.getSession(sessionId);
		final var templateId = ofNullable(renderRequest)
			.map(RenderRequest::templateId)
			.orElseGet(() -> session.getFlow().getDefaultTemplateId());
		final var output = sessionService.renderSession(sessionId, templateId, municipalityId);

		return ok(new Output(output));
	}
}
