package se.sundsvall.ai.flow.api;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.ok;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.ai.flow.api.model.FlowResponse;
import se.sundsvall.ai.flow.api.model.Flows;
import se.sundsvall.ai.flow.model.flow.Flow;
import se.sundsvall.ai.flow.service.FlowService;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;

@RestController
@RequestMapping(value = "/{municipalityId}/flow", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Flows", description = "Flow resources")
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
class FlowResource {

	private final FlowService flowService;

	FlowResource(final FlowService flowService) {
		this.flowService = flowService;
	}

	@Operation(
		summary = "Get all available flows",
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "Ok",
				useReturnTypeSchema = true)
		})
	@GetMapping
	ResponseEntity<Flows> getFlows(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId) {
		return ok(flowService.getFlows());
	}

	@Operation(summary = "Get a flow by name and version",
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
	@GetMapping("/{flowName}/{version}")
	ResponseEntity<FlowResponse> getFlowByNameAndVersion(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "flowName", description = "Flow name", example = "Tjänsteskrivelse") @NotBlank @PathVariable final String flowName,
		@Parameter(name = "version", description = "Flow version", example = "1") @NotNull @PathVariable final Integer version) {
		return ok(flowService.getFlow(flowName, version));
	}

	@Operation(summary = "Delete a flow by name and version",
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
	@DeleteMapping("/{flowName}/{version}")
	ResponseEntity<Void> deleteFlow(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@PathVariable("flowName") final String flowName,
		@PathVariable("version") final int version) {
		flowService.deleteFlow(flowName, version);
		return ok().build();
	}

	@Operation(
		summary = "Create a flow",
		responses = {
			@ApiResponse(
				responseCode = "201",
				description = "Created",
				useReturnTypeSchema = true),
		})
	@PostMapping
	ResponseEntity<Void> createFlow(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@RequestBody final Flow flow) {
		return created(flowService.createFlow(flow))
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}
}
