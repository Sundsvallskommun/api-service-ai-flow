package se.sundsvall.ai.flow.integration.eneo.model;

import generated.eneo.ai.Status;
import java.util.UUID;

public record Response(UUID sessionId, UUID runId, String answer, Status status) {

	// Constructor for SERVICE
	public Response(String answer) {
		this(null, null, answer, null);
	}

	// Constructor for ASSISTANT
	public Response(UUID sessionId, String answer) {
		this(sessionId, null, answer, null);
	}

	// Constructor for APP
	public Response(UUID runId, String answer, Status status) {
		this(null, runId, answer, status);
	}
}
