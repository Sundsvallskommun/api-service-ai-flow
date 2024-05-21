package se.sundsvall.ai.flow.integration.intric.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RunService(@JsonProperty("input") String input) { }
