package se.sundsvall.ai.flow.api.model;

import io.swagger.v3.oas.annotations.media.Schema;

public record RenderRequest(

    @Schema(description = "The template id to use for rendering the session")
    String templateId) { }
