package se.sundsvall.ai.flow.api;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;

import se.sundsvall.ai.flow.api.model.FlowInfo;
import se.sundsvall.ai.flow.model.flow.Flow;
import se.sundsvall.ai.flow.service.FlowRegistry;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(value = "/{municipalityId}/flow", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Flows", description = "Flow resources")
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
class FlowResource {

	private final FlowRegistry flowRegistry;

	FlowResource(final FlowRegistry flowRegistry) {
		this.flowRegistry = flowRegistry;
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
	ResponseEntity<List<FlowInfo>> getAllFlows(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId) {
		var flows = flowRegistry.getAllFlows().stream()
			.map(flow -> new FlowInfo(flow.getId(), flow.getName(), flow.getDescription(), flow.getDefaultTemplateId()))
			.toList();

		return ok(flows);
	}

	@Operation(
		summary = "Get a flow",
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
	@GetMapping("/{flowId}")
	ResponseEntity<Flow> getFlow(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@PathVariable("flowId") final String flowId) {
		return ok(flowRegistry.getFlow(flowId));
	}
}
