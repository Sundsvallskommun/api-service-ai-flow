package se.sundsvall.ai.flow.model.flow;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class FlowInput {

    public enum Cardinality {
        SINGLE_VALUED,
        MULTIPLE_VALUED
    }

    private String id;
    private String name;
    private String description;
    private InputType type;
    private Cardinality cardinality = Cardinality.SINGLE_VALUED;
    private boolean passthrough = false;

    public String getId() {
        return id;
    }

    public FlowInput withId(final String id) {
        this.id = id;
        return this;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public FlowInput withName(final String name) {
        this.name = name;
        return this;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public FlowInput withDescription(final String description) {
        this.description = description;
        return this;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public InputType getType() {
        return type;
    }

    public FlowInput withType(final InputType type) {
        this.type = type;
        return this;
    }

    public void setType(final InputType type) {
        this.type = type;
    }

    @JsonIgnore
    public boolean isSingleValued() {
        return cardinality == Cardinality.SINGLE_VALUED;
    }

    public Cardinality getCardinality() {
        return cardinality;
    }

    public FlowInput withCardinality(final Cardinality cardinality) {
        this.cardinality = cardinality;
        return this;
    }

    public void setCardinality(final Cardinality cardinality) {
        this.cardinality = cardinality;
    }

    public boolean isPassthrough() {
        return passthrough;
    }

    public FlowInput withPassthrough(final boolean passthrough) {
        this.passthrough = passthrough;
        return this;
    }

    public void setPassthrough(final boolean passthrough) {
        this.passthrough = passthrough;
    }
}
