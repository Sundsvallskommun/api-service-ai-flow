package se.sundsvall.ai.flow.api.model;

import io.swagger.v3.oas.annotations.media.Schema;

public record Output(

	@Schema(description = "The BASE64-encoded (binary) data") String data) {}
