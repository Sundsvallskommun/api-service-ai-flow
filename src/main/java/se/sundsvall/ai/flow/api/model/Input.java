package se.sundsvall.ai.flow.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record Input(

	@NotBlank @Schema(description = "The input id", requiredMode = REQUIRED) String inputId,

	@NotBlank @Schema(description = "The value (BASE64-encoded)", requiredMode = REQUIRED, format = "byte") String value) {}
