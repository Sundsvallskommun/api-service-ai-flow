package se.sundsvall.ai.flow.model.session;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Stream;
import org.springframework.web.multipart.MultipartFile;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import se.sundsvall.ai.flow.model.flowdefinition.Flow;
import se.sundsvall.ai.flow.model.flowdefinition.FlowInput;
import se.sundsvall.ai.flow.model.support.ByteArrayMultipartFile;
import se.sundsvall.ai.flow.model.support.StringMultipartFile;

import static java.util.Optional.of;
import static java.util.stream.Collectors.toMap;

public class Session {

	private final UUID id;
	private final String municipalityId;
	@JsonIgnore
	private final Flow flow;
	private final Map<String, List<Input>> input = new TreeMap<>();
	@JsonIgnore
	private final Map<String, List<Input>> redirectedOutputInput = new TreeMap<>();
	private final Map<String, StepExecution> stepExecutions = new TreeMap<>();
	private State state;

	public Session(final String municipalityId, final Flow flow, final StepExecutionFactory stepExecutionFactory) {
		this.municipalityId = municipalityId;
		id = UUID.randomUUID();
		state = State.CREATED;

		this.flow = flow;

		// Create initial (empty) executions for all steps
		stepExecutions.putAll(stepExecutionFactory.createStepExecutions(this, flow));
		// Create initial (empty) input lists
		flow.getFlowInputs().forEach(flowInput -> input.put(flowInput.getId(), new LinkedList<>()));
	}

	// Backwards compatible constructor
	public Session(final String municipalityId, final Flow flow) {
		this(municipalityId, flow, new StepExecutionFactory());
	}

	public String getMunicipalityId() {
		return municipalityId;
	}

	public UUID getId() {
		return id;
	}

	public Flow getFlow() {
		return flow;
	}

	public State getState() {
		return state;
	}

	public void setState(final State state) {
		this.state = state;
	}

	@JsonIgnore
	public LocalDateTime getLastUpdatedAt() {
		return stepExecutions.values().stream()
			.map(StepExecution::getLastUpdatedAt)
			.flatMap(Stream::ofNullable)
			.max(LocalDateTime::compareTo)
			.orElse(null);
	}

	public void addSimpleInput(final String inputId, final String value) {
		final var flowInput = flow.getFlowInput(inputId);
		final var inputMultipartFile = new StringMultipartFile(flowInput.getName(), value);
		addInputInternal(flowInput, inputMultipartFile);
	}

	public void clearInput(final String inputId) {
		final var flowInput = flow.getFlowInput(inputId);

		// Create an empty input value list, if required
		input.computeIfAbsent(flowInput.getId(), ignored -> new LinkedList<>());
		// Clear the input
		input.get(flowInput.getId()).clear();
	}

	private void addInputInternal(final FlowInput flowInput, final MultipartFile inputMultipartFile) {
		// Create an empty input value list, if required
		input.computeIfAbsent(flowInput.getId(), ignored -> new LinkedList<>());
		// If the flow input is single-valued - replace the previous value by clearing any previous value(s)
		if (flowInput.isSingleValued()) {
			input.get(flowInput.getId()).clear();
		}
		// Add the input
		input.get(flowInput.getId()).add(new Input(inputMultipartFile));
	}

	public Map<String, List<Input>> getInput() {
		// Wrap with unmodifiable collections to avoid external mutation
		final var wrapped = input.entrySet().stream()
			.collect(toMap(Map.Entry::getKey, e -> Collections.unmodifiableList(e.getValue())));
		return Collections.unmodifiableMap(wrapped);
	}

	public void addInput(final String inputId, final InputValue inputValue) {
		final var flowInput = flow.getFlowInput(inputId);
		final var flowInputName = flowInput.getName();

		if (inputValue instanceof final TextInputValue textInputValue) {
			addInputInternal(flowInput, new StringMultipartFile(flowInputName, textInputValue.value()));
		} else if (inputValue instanceof final FileInputValue fileInputValue) {
			addInputInternal(flowInput, new ByteArrayMultipartFile(flowInputName, fileInputValue.content(), fileInputValue.contentType()));
		} else {
			throw new IllegalArgumentException("Unsupported InputValue implementation: " + inputValue.getClass());
		}
	}

	public void addRedirectedOutputAsInput(final String stepId, final InputValue inputValue) {
		redirectedOutputInput.computeIfAbsent(stepId, ignored -> new LinkedList<>());

		if (inputValue instanceof TextInputValue(final String name, final String value)) {
			var textInput = new Input(new StringMultipartFile(name, value));
			redirectedOutputInput.get(stepId).add(textInput);
		} else if (inputValue instanceof FileInputValue(final String name, final byte[] content, final String contentType)) {
			var fileInput = new Input(new ByteArrayMultipartFile(name, content, contentType));
			redirectedOutputInput.get(stepId).add(fileInput);
		} else {
			throw new IllegalArgumentException("Unsupported InputValue implementation: " + inputValue.getClass());
		}
	}

	@JsonIgnore
	public Map<String, List<Input>> getRedirectedOutputInput() {
		final var wrapped = redirectedOutputInput.entrySet().stream()
			.collect(toMap(Map.Entry::getKey, e -> Collections.unmodifiableList(e.getValue())));
		return Collections.unmodifiableMap(wrapped);
	}

	/**
	 * Removes a specific redirected output input instance for a given source step id. This provides a controlled mutation
	 * entry point since the public view is unmodifiable.
	 */
	public void removeRedirectedOutputInput(final String sourceStepId, final Input inputToRemove) {
		final var list = redirectedOutputInput.get(sourceStepId);
		if (list != null) {
			list.remove(inputToRemove);
		}
	}

	@JsonIgnore
	public Map<String, List<Input>> getAllInput() {
		final var merged = Stream.concat(input.entrySet().stream(), redirectedOutputInput.entrySet().stream())
			.collect(toMap(Map.Entry::getKey, e -> Collections.unmodifiableList(e.getValue())));
		return Collections.unmodifiableMap(merged);
	}

	public Map<String, StepExecution> getStepExecutions() {
		return stepExecutions;
	}

	public StepExecution getStepExecution(final String stepId) {
		return of(stepExecutions)
			.map(actualStepExecutions -> actualStepExecutions.get(stepId))
			.orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND, "No step execution exists for step '%s' in session '%s'".formatted(stepId, id)));
	}

	public enum State {
		CREATED,
		RUNNING,
		FINISHED,
		ERROR
	}
}
