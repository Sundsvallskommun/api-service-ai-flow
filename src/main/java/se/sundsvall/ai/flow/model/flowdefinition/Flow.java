package se.sundsvall.ai.flow.model.flowdefinition;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

public class Flow {

	private String id;
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private Integer version;
	private String name;
	private String description;
	private String helptext;
	private String spaceId;
	private String defaultTemplateId;
	private int ttlInMinutes = 180;

	@JsonProperty("input")
	private List<FlowInput> flowInputs = new LinkedList<>();
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

	public Integer getVersion() {
		return version;
	}

	public void setVersion(final Integer version) {
		this.version = version;
	}

	public Flow withVersion(final Integer version) {
		this.version = version;
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

	public String getHelptext() {
		return helptext;
	}

	public void setHelptext(final String helptext) {
		this.helptext = helptext;
	}

	public Flow withHelptext(final String helptext) {
		this.helptext = helptext;
		return this;
	}

	public String getSpaceId() {
		return spaceId;
	}

	public void setSpaceId(final String spaceId) {
		this.spaceId = spaceId;
	}

	public Flow withSpaceId(final String spaceId) {
		this.spaceId = spaceId;
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

	public int getTtlInMinutes() {
		return ttlInMinutes;
	}

	public void setTtlInMinutes(final int ttlInMinutes) {
		this.ttlInMinutes = ttlInMinutes;
	}

	public Flow withTtlInMinutes(final int ttlInMinutes) {
		this.ttlInMinutes = ttlInMinutes;
		return this;
	}

	public FlowInput getFlowInput(final String inputId) {
		return flowInputs.stream()
			.filter(currentFlowInput -> inputId.equals(currentFlowInput.getId()))
			.findFirst()
			.orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND, "No input '%s' exists in flow '%s'".formatted(inputId, name)));
	}

	public List<FlowInput> getFlowInputs() {
		return flowInputs;
	}

	public void setFlowInputs(final List<FlowInput> flowInputs) {
		this.flowInputs = flowInputs;
	}

	public Flow withFlowInputs(final List<FlowInput> inputs) {
		this.flowInputs = inputs;
		return this;
	}

	public Step getStep(final String stepId) {
		return steps.stream()
			.filter(currentStep -> stepId.equals(currentStep.getId()))
			.findFirst()
			.orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND, "No step '%s' exists in flow '%s'".formatted(stepId, name)));
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

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof final Flow other)) {
			return false;
		}
		return Objects.equals(id, other.id) && Objects.equals(version, other.version);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, version);
	}
}
