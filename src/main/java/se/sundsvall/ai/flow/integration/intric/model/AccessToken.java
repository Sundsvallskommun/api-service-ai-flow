package se.sundsvall.ai.flow.integration.intric.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AccessToken(

    @JsonProperty("access_token")
    String accessToken,
    @JsonProperty("token_type")
    String tokenType) { }

