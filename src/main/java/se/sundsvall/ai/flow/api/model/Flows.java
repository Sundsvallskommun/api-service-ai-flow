package se.sundsvall.ai.flow.api.model;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record Flows(

	@ArraySchema(schema = @Schema(implementation = FlowSummary.class)) List<FlowSummary> flows) {
}
