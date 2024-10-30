package se.sundsvall.ai.flow.service.flow;

import java.util.UUID;

public class FlowExecution {

	private final UUID id;
	private ExecutionState state;

	public FlowExecution() {
		id = UUID.randomUUID();

		state = ExecutionState.PENDING;
	}

	public UUID getId() {
		return id;
	}

	public ExecutionState getState() {
		return state;
	}

	public FlowExecution withState(final ExecutionState state) {
		this.state = state;
		return this;
	}

	public void setState(final ExecutionState state) {
		this.state = state;
	}
}
