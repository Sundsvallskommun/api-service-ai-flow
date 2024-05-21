package se.sundsvall.ai.flow.model.flow;

import static java.util.stream.Collectors.toMap;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Flow {

    private String id;
    private String name;
    private String description;
    private String inputPrefix = "#####";
    private String defaultTemplateId;

    @JsonProperty("input")
    private final List<FlowInput> inputs = new LinkedList<>();
    @JsonProperty("steps")
    private final List<Step> steps = new LinkedList<>();
    @JsonIgnore
    private final Map<String, Step> stepMap = new LinkedHashMap<>();

    public String getId() {
        return id;
    }

    public Flow withId(final String id) {
        this.id = id;
        return this;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Flow withName(final String name) {
        this.name = name;
        return this;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public Flow withDescription(final String description) {
        this.description = description;
        return this;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getInputPrefix() {
        return inputPrefix;
    }

    public Flow withInputPrefix(final String inputPrefix) {
        this.inputPrefix = inputPrefix;
        return this;
    }

    public void setInputPrefix(final String inputPrefix) {
        this.inputPrefix = inputPrefix;
    }

    public String getDefaultTemplateId() {
        return defaultTemplateId;
    }

    public Flow withDefaultTemplatIde(final String defaultTemplateId) {
        this.defaultTemplateId = defaultTemplateId;
        return this;
    }

    public void setDefaultTemplateId(final String defaultTemplateId) {
        this.defaultTemplateId = defaultTemplateId;
    }

    public Step getStep(final String stepId) {
        return stepMap.get(stepId);
    }

    public List<Step> getSteps() {
        return steps;
    }

    public Flow withSteps(final Step...stepsToSet) {
        for (var step : stepsToSet) {
            steps.add(step);
            stepMap.put(step.getId(), step);
        }
        return this;
    }

    public void setSteps(final List<Step> steps) {
        this.steps.clear();
        this.steps.addAll(steps);

        stepMap.clear();
        stepMap.putAll(steps.stream().collect(toMap(Step::getId, Function.identity())));
    }

    public List<FlowInput> getInputs() {
        return inputs;
    }

    @JsonIgnore
    public Map<String, FlowInput> getInputMap() {
        return inputs.stream()
            .collect(toMap(FlowInput::getId, Function.identity()));
    }

    public Flow withInputs(final FlowInput...inputsToSet) {
        for (var input : inputsToSet) {
            inputs.add(input);
        }
        return this;
    }

    public void setInputs(final List<FlowInput> inputs) {
        this.inputs.clear();
        this.inputs.addAll(inputs);
    }
}
