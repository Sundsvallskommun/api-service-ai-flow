package se.sundsvall.ai.flow.api.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Flow info")
public record FlowInfo(
	@Schema(description = "The flow id") String id,

	@Schema(description = "The flow name") String name,

	@Schema(description = "The flow description", nullable = true) String description,

	@Schema(description = "The default template id", nullable = true) String defaultTemplateId) {}
