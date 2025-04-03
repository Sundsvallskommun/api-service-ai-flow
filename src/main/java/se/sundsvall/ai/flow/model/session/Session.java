package se.sundsvall.ai.flow.model.session;

import static java.util.Optional.of;
import static java.util.stream.Collectors.toMap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import se.sundsvall.ai.flow.model.flowdefinition.Flow;
import se.sundsvall.ai.flow.model.flowdefinition.FlowInput;
import se.sundsvall.ai.flow.model.flowdefinition.RedirectedOutput;
import se.sundsvall.ai.flow.model.flowdefinition.Step;
import se.sundsvall.ai.flow.model.support.StringMultipartFile;
import se.sundsvall.ai.flow.model.support.UploadedMultipartFile;

public class Session {

	private static final Logger LOG = LoggerFactory.getLogger(Session.class);

	static final String FIlE_INFO_TEMPLATE = "Du hittar %s i filen/filerna %s. ";

	public enum State {
		CREATED,
		RUNNING,
		FINISHED
	}

	private final UUID id;
	@JsonIgnore
	private final Flow flow;
	private final Map<String, List<Input>> input = new TreeMap<>();
	@JsonIgnore
	private final Map<String, List<Input>> redirectedOutputInput = new TreeMap<>();
	private final Map<String, StepExecution> stepExecutions = new TreeMap<>();
	private State state;

	public Session(final Flow flow) {
		id = UUID.randomUUID();
		state = State.CREATED;

		this.flow = flow;

		// Create initial (empty) executions for all steps
		flow.getSteps().forEach(this::createStepExecution);
		// Create initial (empty) input lists
		flow.getFlowInputs().forEach(flowInput -> input.put(flowInput.getId(), new LinkedList<>()));
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
		var flowInput = flow.getFlowInput(inputId);
		var inputMultipartFile = new StringMultipartFile(flow.getInputPrefix(), flowInput.getName(), value);

		addInput(flowInput, inputMultipartFile);
	}

	public void addFileInput(final String inputId, final MultipartFile inputMultipartFile) {
		var flowInput = flow.getFlowInput(inputId);
		var uploadedInputMultipartFile = new UploadedMultipartFile(flowInput.getName(), inputMultipartFile);

		addInput(flowInput, uploadedInputMultipartFile);
	}

	public void clearInput(final String inputId) {
		var flowInput = flow.getFlowInput(inputId);

		// Create an empty input value list, if required
		input.computeIfAbsent(flowInput.getId(), ignored -> new LinkedList<>());
		// Clear the input
		input.get(flowInput.getId()).clear();
	}

	void addInput(final FlowInput flowInput, final MultipartFile inputMultipartFile) {
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
		return input;
	}

	public void addRedirectedOutputAsInput(final String stepId, final MultipartFile redirectedOutputMultipartFile) {
		// Add the input
		redirectedOutputInput.put(stepId, new LinkedList<>(List.of(new Input(redirectedOutputMultipartFile))));
	}

	@JsonIgnore
	public Map<String, List<Input>> getRedirectedOutputInput() {
		return redirectedOutputInput;
	}

	@JsonIgnore
	public Map<String, List<Input>> getAllInput() {
		return Stream.concat(input.entrySet().stream(), redirectedOutputInput.entrySet().stream())
			.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	@JsonIgnore
	public Map<String, String> getInputInfo() {
		// Extract/create info on regular inputs
		var regularInputInfo = input.entrySet().stream()
			.map(entry -> {
				var inputId = entry.getKey();
				// Get the flow input corresponding to this input
				var flowInput = flow.getFlowInput(inputId);

				return createInputInfo(inputId, flowInput.getName(), entry.getValue());
			})
			.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

		// Extract/create info on redirected output inputs
		var redirectedOutputInputInfo = redirectedOutputInput.entrySet().stream()
			.map(entry -> {
				var stepId = entry.getKey();
				// Get the redirected step
				var step = flow.getStep(stepId);

				return createInputInfo(stepId, step.getName(), entry.getValue());
			})
			.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

		return Stream.concat(regularInputInfo.entrySet().stream(), redirectedOutputInputInfo.entrySet().stream())
			.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	AbstractMap.SimpleEntry<String, String> createInputInfo(final String key, final String name, final List<Input> inputs) {
		// Extract the Intric uploaded file ids
		var intricFileIds = inputs.stream().map(Input::getIntricFileId).map(UUID::toString).toList();
		// Format the info
		var info = String.format(FIlE_INFO_TEMPLATE, name.toLowerCase(), String.join(",", intricFileIds));

		return new AbstractMap.SimpleEntry<>(key, info);
	}

	StepExecution createStepExecution(final Step step) {
		LOG.info("Creating step execution for step '{}' from flow '{}' for session {}", step.getName(), flow.getName(), id);

		// Validate redirected output inputs
		var requiredStepExecutions = new ArrayList<StepExecution>();
		for (var stepInput : step.getInputs()) {
			if (stepInput instanceof RedirectedOutput redirectedOutput) {
				// Make sure required step(s) have been executed before this one
				var sourceStepId = redirectedOutput.getStep();
				var sourceStep = flow.getStep(sourceStepId);

				if (!stepExecutions.containsKey(sourceStepId)/* || isBlank(stepExecutions.get(sourceStepId).getOutput()) */) {
					LOG.info("Creating step execution for missing redirected output from step '{}' for step '{}' in flow '{}' for session {}", sourceStepId, step.getId(), flow.getName(), id);

					requiredStepExecutions.add(createStepExecution(sourceStep));
				}
			}
		}

		LOG.info("Created step execution for step '{}' from flow '{}' for session {}", step.getName(), flow.getName(), id);

		var stepExecution = new StepExecution(this, step, requiredStepExecutions);
		stepExecutions.put(step.getId(), stepExecution);
		return stepExecution;
	}

	public Map<String, StepExecution> getStepExecutions() {
		return stepExecutions;
	}

	public StepExecution getStepExecution(final String stepId) {
		return of(stepExecutions)
			.map(actualStepExecutions -> actualStepExecutions.get(stepId))
			.orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND, "No step execution exists for step '%s' in session '%s'".formatted(stepId, id)));
	}
}
