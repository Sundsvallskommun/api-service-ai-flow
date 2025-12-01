package se.sundsvall.ai.flow.model.session;

import static java.util.Collections.emptyList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import se.sundsvall.ai.flow.model.flowdefinition.Step;

public class StepExecution implements Comparable<StepExecution> {

	@JsonIgnore
	private final UUID id;
	@JsonIgnore
	private final Session session;
	@JsonIgnore
	private final Step step;
	@JsonIgnore
	private final List<StepExecution> requiredStepExecutions;
	@JsonIgnore
	private UUID eneoSessionId;
	@JsonIgnore
	private UUID eneoRunId;
	private State state;
	private LocalDateTime startedAt;
	private LocalDateTime lastUpdatedAt;
	private LocalDateTime finishedAt;
	private String output;
	private String errorMessage;

	public StepExecution(final Session session, final Step step) {
		this(session, step, emptyList());
	}

	public StepExecution(final Session session, final Step step, final List<StepExecution> requiredStepExecutions) {
		id = UUID.randomUUID();

		setState(State.CREATED);

		this.session = session;
		this.step = step;
		this.requiredStepExecutions = requiredStepExecutions;
	}

	public UUID getId() {
		return id;
	}

	public Session getSession() {
		return session;
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

	public LocalDateTime getLastUpdatedAt() {
		return lastUpdatedAt;
	}

	public LocalDateTime getFinishedAt() {
		return finishedAt;
	}

	public State getState() {
		return state;
	}

	public void setState(final State state) {
		this.state = state;

		// Set either the startedAt or the finishedAt timestamp depending on the state
		if (state == State.RUNNING) {
			startedAt = LocalDateTime.now();
		} else if (state == State.DONE || state == State.ERROR) {
			finishedAt = LocalDateTime.now();
		}
		// Update the lastUpdatedAt timestamp
		lastUpdatedAt = LocalDateTime.now();
	}

	@JsonIgnore
	public boolean isRunning() {
		return state == State.RUNNING;
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

	public UUID getEneoSessionId() {
		return eneoSessionId;
	}

	public void setEneoSessionId(final UUID eneoSessionId) {
		this.eneoSessionId = eneoSessionId;
	}

	public UUID getEneoRunId() {
		return eneoRunId;
	}

	public void setEneoRunId(final UUID eneoRunId) {
		this.eneoRunId = eneoRunId;
	}

	@Override
	public int compareTo(final StepExecution other) {
		return step.compareTo(other.step);
	}

	public enum State {
		CREATED,
		RUNNING,
		DONE,
		ERROR
	}
}
