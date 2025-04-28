package se.sundsvall.ai.flow.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ChatRequest(

	@NotBlank @Schema(description = "The input", requiredMode = Schema.RequiredMode.REQUIRED) String input,

	@Schema(description = "Whether to run/re-run required steps", defaultValue = "false") boolean runRequiredSteps) {}
