package se.sundsvall.ai.flow.model.flow;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

public class Step implements Comparable<Step> {

	private String id;
	private int order;
	private String name;
	private String description;
	private String intricServiceId;
	@JsonProperty("input")
	private List<Input> inputs;

	public String getId() {
		return id;
	}

	public Step withId(final String id) {
		this.id = id;
		return this;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public int getOrder() {
		return order;
	}

	public Step withOrder(final int order) {
		this.order = order;
		return this;
	}

	public void setOrder(final int order) {
		this.order = order;
	}

	public String getName() {
		return name;
	}

	public Step withName(final String name) {
		this.name = name;
		return this;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public Step withDescription(final String description) {
		this.description = description;
		return this;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public String getIntricServiceId() {
		return intricServiceId;
	}

	public Step withIntricServiceId(final String intricServiceId) {
		this.intricServiceId = intricServiceId;
		return this;
	}

	public void setIntricServiceId(final String intricServiceId) {
		this.intricServiceId = intricServiceId;
	}

	public List<Input> getInputs() {
		return inputs;
	}

	public Step withInputs(final List<Input> inputs) {
		this.inputs = inputs;
		return this;
	}

	public void setInputs(final List<Input> inputs) {
		this.inputs = inputs;
	}

	@Override
	public int compareTo(@NotNull final Step other) {
		return Integer.compare(order, other.order);
	}
}
