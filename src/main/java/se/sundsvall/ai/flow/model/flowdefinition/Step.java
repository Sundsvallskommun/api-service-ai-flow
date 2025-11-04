package se.sundsvall.ai.flow.model.flowdefinition;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public class Step implements Comparable<Step> {

	private String id;
	private int order;
	private String name;
	private String description;
	private Target target;
	@JsonProperty("input")
	private List<StepInput> inputs = new LinkedList<>();

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public Step withId(final String id) {
		this.id = id;
		return this;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(final int order) {
		this.order = order;
	}

	public Step withOrder(final int order) {
		this.order = order;
		return this;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public Step withName(final String name) {
		this.name = name;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public Step withDescription(final String description) {
		this.description = description;
		return this;
	}

	public Target getTarget() {
		return target;
	}

	public void setTarget(final Target target) {
		this.target = target;
	}

	public Step withTarget(final Target target) {
		this.target = target;
		return this;
	}

	public List<StepInput> getInputs() {
		return inputs;
	}

	public void setInputs(final List<StepInput> inputs) {
		this.inputs = inputs;
	}

	public Step withInputs(final List<StepInput> inputs) {
		this.inputs = inputs;
		return this;
	}

	@Override
	public int compareTo(@NotNull final Step other) {
		return Integer.compare(order, other.order);
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass())
			return false;
		final Step step = (Step) o;
		return order == step.order && Objects.equals(id, step.id) && Objects.equals(name, step.name) && Objects.equals(description, step.description) && Objects.equals(target, step.target) && Objects.equals(
			inputs, step.inputs);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, order, name, description, target, inputs);
	}

	public record Target(Type type, UUID id) {

		public enum Type {
			SERVICE,
			ASSISTANT
		}
	}
}
