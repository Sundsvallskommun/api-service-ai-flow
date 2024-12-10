package se.sundsvall.ai.flow.model;

import static com.knuddels.jtokkit.Encodings.newDefaultEncodingRegistry;
import static com.knuddels.jtokkit.api.EncodingType.CL100K_BASE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static se.sundsvall.ai.flow.util.DocumentUtil.extractTextFromDocx;
import static se.sundsvall.ai.flow.util.DocumentUtil.extractTextFromPdf;
import static se.sundsvall.ai.flow.util.DocumentUtil.isDocx;
import static se.sundsvall.ai.flow.util.DocumentUtil.isPdf;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.knuddels.jtokkit.api.Encoding;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import se.sundsvall.ai.flow.model.flow.Flow;
import se.sundsvall.ai.flow.model.flow.InputType;
import se.sundsvall.ai.flow.service.flow.StepExecution;

public class Session {

	private static final Encoding ENCODING = newDefaultEncodingRegistry().getEncoding(CL100K_BASE);

	public enum State {
		CREATED,
		RUNNING,
		FINISHED
	}

	private final UUID id;
	@JsonIgnore
	private Flow flow;
	private State state;
	private int tokenCount;

	private Map<String, List<String>> input;
	private Map<String, StepExecution> stepExecutions;

	public Session() {
		id = UUID.randomUUID();
		state = State.CREATED;
	}

	public UUID getId() {
		return id;
	}

	public Flow getFlow() {
		return flow;
	}

	public Session withFlow(final Flow flow) {
		this.flow = flow;
		return this;
	}

	public void setFlow(final Flow flow) {
		this.flow = flow;
	}

	public State getState() {
		return state;
	}

	public Session withState(final State state) {
		this.state = state;
		return this;
	}

	public void setState(final State state) {
		this.state = state;
	}

	public void addInput(final String inputId, final String value) {
		addOrReplaceInput(inputId, value, false);
	}

	public void replaceInput(final String inputId, final String value) {
		addOrReplaceInput(inputId, value, true);
	}

	void addOrReplaceInput(final String inputId, final String value, final boolean replace) {
		// Make sure the corresponding flow input exists
		var flowInput = ofNullable(flow.getInputMap().get(inputId))
			.orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND, "No input '%s' exists in flow '%s'".formatted(inputId, flow.getName())));

		// BASE64-decode the value
		var valueBytes = Base64.getDecoder().decode(value);

		String decodedValue;
		// Extract the document text if we're dealing with a document (PDF or DOCX)
		if (flowInput.getType() == InputType.DOCUMENT) {
			if (isDocx(valueBytes)) {
				decodedValue = extractTextFromDocx(valueBytes);
			} else if (isPdf(valueBytes)) {
				decodedValue = extractTextFromPdf(valueBytes);
			} else {
				throw Problem.valueOf(Status.BAD_REQUEST, "Document input '%s' does not appear to be either a PDF or Word document".formatted(inputId));
			}
		} else {
			decodedValue = new String(valueBytes, UTF_8);
		}

		// Lazy-init the input map, if required
		if (input == null) {
			input = new ConcurrentHashMap<>();
		}
		// Create an empty input value list, if required
		if (!input.containsKey(inputId)) {
			input.put(inputId, new LinkedList<>());
		}

		var inputValue = input.get(inputId);

		// If we're either replacing the input, or if the flow input is single-valued - replace the
		// previous value by clearing any previous value(s)
		if (replace || flowInput.isSingleValued()) {
			inputValue.clear();
		}
		inputValue.add(decodedValue);

		// Update the token count
		updateTokenCount();
	}

	public Map<String, List<String>> getInput() {
		return input;
	}

	public boolean hasStepOutput(final String stepId) {
		return stepExecutions.containsKey(stepId) && isNotBlank(stepExecutions.get(stepId).getOutput());
	}

	public void addStepExecution(final String stepId, final StepExecution stepExecution) {
		if (stepExecutions == null) {
			stepExecutions = new ConcurrentHashMap<>();
		}

		stepExecutions.put(stepId, stepExecution);
	}

	public Map<String, StepExecution> getStepExecutions() {
		return stepExecutions;
	}

	public StepExecution getStepExecution(final String stepId) {
		return ofNullable(stepExecutions)
			.map(stepExecutions -> stepExecutions.get(stepId))
			.orElse(null);
	}

	public int getTokenCount() {
		return tokenCount;
	}

	private void updateTokenCount() {
		var actualInput = ofNullable(input)
			.map(Map::values)
			.stream()
			.flatMap(Collection::stream)
			.flatMap(Collection::stream)
			.collect(Collectors.joining(" "));

		tokenCount = ENCODING.countTokens(actualInput);
	}
}
