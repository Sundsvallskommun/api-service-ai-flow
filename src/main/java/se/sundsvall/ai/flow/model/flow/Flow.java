package se.sundsvall.ai.flow.model.flow;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.LinkedList;
import java.util.List;

public class Flow {

	private String id;
	private String name;
	private String description;
	private String inputPrefix = "#####";
	private String defaultTemplateId;

	@JsonProperty("input")
	private List<FlowInput> inputs = new LinkedList<>();
	@JsonProperty("steps")
	private List<Step> steps = new LinkedList<>();

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public Flow withId(final String id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public Flow withName(final String name) {
		this.name = name;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public Flow withDescription(final String description) {
		this.description = description;
		return this;
	}

	public String getInputPrefix() {
		return inputPrefix;
	}

	public void setInputPrefix(final String inputPrefix) {
		this.inputPrefix = inputPrefix;
	}

	public Flow withInputPrefix(final String inputPrefix) {
		this.inputPrefix = inputPrefix;
		return this;
	}

	public String getDefaultTemplateId() {
		return defaultTemplateId;
	}

	public void setDefaultTemplateId(final String defaultTemplateId) {
		this.defaultTemplateId = defaultTemplateId;
	}

	public Flow withDefaultTemplateId(final String defaultTemplateId) {
		this.defaultTemplateId = defaultTemplateId;
		return this;
	}

	public List<FlowInput> getInputs() {
		return inputs;
	}

	public void setInputs(final List<FlowInput> inputs) {
		this.inputs = inputs;
	}

	public Flow withInputs(final List<FlowInput> inputs) {
		this.inputs = inputs;
		return this;
	}

	public List<Step> getSteps() {
		return steps;
	}

	public void setSteps(final List<Step> steps) {
		this.steps = steps;
	}

	public Flow withSteps(final List<Step> steps) {
		this.steps = steps;
		return this;
	}
}
