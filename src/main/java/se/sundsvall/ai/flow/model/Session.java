package se.sundsvall.ai.flow.model;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static se.sundsvall.ai.flow.util.DocumentUtil.extractTextFromDocx;
import static se.sundsvall.ai.flow.util.DocumentUtil.extractTextFromPdf;
import static se.sundsvall.ai.flow.util.DocumentUtil.isDocx;
import static se.sundsvall.ai.flow.util.DocumentUtil.isPdf;

import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import se.sundsvall.ai.flow.model.flow.Flow;
import se.sundsvall.ai.flow.model.flow.InputType;
import se.sundsvall.ai.flow.service.flow.StepExecution;

public class Session {

    public enum State {
        CREATED,
        RUNNING,
        FINISHED
    }

    private final UUID id;
    @JsonIgnore
    private Flow flow;
    private State state;

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
        var decodedValue = new String(Base64.getDecoder().decode(value), UTF_8);
        // Extract the document text if we're dealing with a document (PDF or DOCX)
        if (flowInput.getType() == InputType.DOCUMENT) {
            var decodedValueBytes = decodedValue.getBytes(UTF_8);
            if (isDocx(decodedValueBytes)) {
                decodedValue = extractTextFromDocx(decodedValueBytes);
            } else if (isPdf(decodedValueBytes)) {
                decodedValue = extractTextFromPdf(decodedValueBytes);
            } else {
                throw Problem.valueOf(Status.BAD_REQUEST, "Unable to parse value for document input '%s'");
            }
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
}
