package se.sundsvall.ai.flow.api;

import static java.util.Optional.ofNullable;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.util.UriComponentsBuilder.fromPath;
import static org.zalando.problem.Status.NOT_FOUND;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.ai.flow.api.model.Input;
import se.sundsvall.ai.flow.api.model.Output;
import se.sundsvall.ai.flow.api.model.RenderRequest;
import se.sundsvall.ai.flow.model.Session;
import se.sundsvall.ai.flow.service.SessionService;
import se.sundsvall.ai.flow.service.StepExecutor;
import se.sundsvall.ai.flow.service.flow.StepExecution;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;

@RestController
@RequestMapping(value = "/{municipalityId}/session", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Sessions", description = "Session resources")
@ApiResponse(
	responseCode = "400",
	description = "Bad Request",
	content = @Content(schema = @Schema(oneOf = {
		Problem.class, ConstraintViolationProblem.class
	})))
@ApiResponse(
	responseCode = "500",
	description = "Internal Server Error",
	content = @Content(schema = @Schema(implementation = Problem.class)))
class SessionResource {

	private final SessionService sessionService;

	private final StepExecutor stepExecutor;

	SessionResource(final SessionService sessionService, final StepExecutor stepExecutor) {
		this.sessionService = sessionService;
		this.stepExecutor = stepExecutor;
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
	@GetMapping("/{sessionId}")
	ResponseEntity<Session> getSession(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@PathVariable("sessionId") final UUID sessionId) {
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
				content = @Content(schema = @Schema(implementation = Problem.class)))
		})
	@PostMapping("/{flowName}/{version}")
	ResponseEntity<Session> createSession(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "flowName", description = "Flow name", example = "Tjänsteskrivelse") @NotBlank @PathVariable final String flowName,
		@Parameter(name = "version", description = "Flow version", example = "1") @NotNull @PathVariable final Integer version) {
		var session = sessionService.createSession(flowName, version);

		return created(fromPath("/session/{sessionId}").buildAndExpand(session.getId()).toUri())
			.body(session);
	}

	@Operation(
		summary = "Add session input",
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
	@PostMapping("/{sessionId}")
	ResponseEntity<Session> addInput(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@PathVariable("sessionId") final UUID sessionId,
		@RequestBody @Valid final Input input) {
		return ok(sessionService.addInput(sessionId, input.inputId(), input.value()));
	}

	@Operation(
		summary = "Replace session input",
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
	@PutMapping("/{sessionId}")
	ResponseEntity<Session> replaceInput(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@PathVariable("sessionId") final UUID sessionId,
		@RequestBody @Valid final Input input) {
		return ok(sessionService.replaceInput(sessionId, input.inputId(), input.value()));
	}

	@Operation(
		summary = "Get a step execution",
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
	@GetMapping("/{sessionId}/{stepId}")
	ResponseEntity<StepExecution> getStepExecution(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@PathVariable("sessionId") final UUID sessionId,
		@PathVariable("stepId") final String stepId) {
		var session = sessionService.getSession(sessionId);
		var stepExecution = Optional.ofNullable(session.getStepExecution(stepId))
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "No step execution exists for step '%s' in flow '%s' for session %s".formatted(stepId, session.getFlow().getName(), sessionId)));

		return ok(stepExecution);
	}

	@Operation(
		summary = "Run a step in a session",
		responses = {
			@ApiResponse(
				responseCode = "201",
				description = "Ok",
				useReturnTypeSchema = true),
			@ApiResponse(
				responseCode = "404",
				description = "Not Found",
				content = @Content(schema = @Schema(implementation = Problem.class)))
		})
	@PostMapping("/run/{sessionId}/{stepId}")
	ResponseEntity<StepExecution> runStep(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@PathVariable("sessionId") final UUID sessionId,
		@PathVariable("stepId") final String stepId) {
		var session = sessionService.getSession(sessionId);
		var flow = session.getFlow();

		// Make sure the step isn't already running
		var stepExecution = session.getStepExecution(stepId);
		if (stepExecution != null && stepExecution.isRunning()) {
			throw Problem.valueOf(Status.BAD_REQUEST, "Unable to run already running step '%s' in flow '%s' for session %s".formatted(stepId, flow.getName(), sessionId));
		}

		stepExecution = sessionService.createStepExecution(sessionId, stepId);

		stepExecutor.executeStep(stepExecution);

		return created(fromPath("/session/{sessionId}/{stepId}/{executionId}")
			.buildAndExpand(sessionId, stepId, stepExecution.getId()).toUri())
			.body(stepExecution);
	}

	@PostMapping("/{sessionId}/generate")
	ResponseEntity<Output> generateSessionOutput(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@PathVariable("sessionId") final UUID sessionId,
		@RequestBody(required = false) @Valid final RenderRequest renderRequest) {
		var session = sessionService.getSession(sessionId);
		var templateId = ofNullable(renderRequest)
			.map(RenderRequest::templateId)
			.orElseGet(() -> session.getFlow().getDefaultTemplateId());
		var output = sessionService.renderSession(sessionId, templateId, municipalityId);

		return ok(new Output(output));
	}

}
