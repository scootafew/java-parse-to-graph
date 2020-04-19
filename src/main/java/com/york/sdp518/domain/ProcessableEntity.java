package com.york.sdp518.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public abstract class ProcessableEntity extends Entity {

    /**
     * Currently nodes could get stuck IN_PROGRESS, possible solution is to add TTL
     * https://neo4j.com/docs/labs/apoc/current/graph-updates/ttl/
     */
    ProcessingState processingState = ProcessingState.NOT_PROCESSED;

    ProcessableEntity() {
        super();
    }

    ProcessableEntity(String fullyQualifiedName, String name) {
        super(fullyQualifiedName, name);
    }

    public ProcessingState getProcessingState() {
        return processingState;
    }

    public void setProcessingState(ProcessingState processingState) {
        this.processingState = processingState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ProcessableEntity that = (ProcessableEntity) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(processingState, that.processingState)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(processingState)
                .toHashCode();
    }
}
