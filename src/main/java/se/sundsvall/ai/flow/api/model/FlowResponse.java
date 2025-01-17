package se.sundsvall.ai.flow.api.model;

import io.swagger.v3.oas.annotations.media.Schema;

public record FlowResponse(

	@Schema(description = "The flow name") String name,

	@Schema(description = "The flow version") Integer version,

	@Schema(description = "The flow content") String content) {
}
