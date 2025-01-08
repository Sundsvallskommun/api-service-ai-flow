package se.sundsvall.ai.flow.service.flow;

import static se.sundsvall.ai.flow.service.flow.ExecutionState.DONE;
import static se.sundsvall.ai.flow.service.flow.ExecutionState.ERROR;
import static se.sundsvall.ai.flow.service.flow.ExecutionState.RUNNING;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import se.sundsvall.ai.flow.model.flow.Step;

public class StepExecution implements Comparable<StepExecution> {

	@JsonIgnore
	private final UUID id;
	@JsonIgnore
	private final UUID sessionId;
	@JsonIgnore
	private final Step step;
	@JsonIgnore
	private final List<StepExecution> requiredStepExecutions;

	private final LocalDateTime startedAt = LocalDateTime.now();
	private LocalDateTime finishedAt;
	private ExecutionState state;
	private String output;
	private String errorMessage;

	public StepExecution(final UUID sessionId, final Step step, final List<StepExecution> requiredStepExecutions) {
		state = ExecutionState.PENDING;

		this.id = UUID.randomUUID();
		this.sessionId = sessionId;
		this.step = step;
		this.requiredStepExecutions = requiredStepExecutions;
	}

	public UUID getId() {
		return id;
	}

	public UUID getSessionId() {
		return sessionId;
	}

	public Step getStep() {
		return step;
	}

	public List<StepExecution> getRequiredStepExecutions() {
		return requiredStepExecutions;
	}

	public LocalDateTime getStartedAt() {
		return startedAt;
	}

	public LocalDateTime getFinishedAt() {
		return finishedAt;
	}

	public ExecutionState getState() {
		return state;
	}

	public StepExecution withState(final ExecutionState state) {
		this.state = state;
		return this;
	}

	public void setState(final ExecutionState state) {
		this.state = state;

		if (state == DONE || state == ERROR) {
			finishedAt = LocalDateTime.now();
		}
	}

	@JsonIgnore
	public boolean isRunning() {
		return state == RUNNING;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(final String output) {
		this.output = output;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(final String errorMessage) {
		this.errorMessage = errorMessage;
	}

	@Override
	public int compareTo(final StepExecution other) {
		return startedAt.compareTo(other.startedAt);
	}
}
