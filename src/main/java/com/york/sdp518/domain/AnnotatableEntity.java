package com.york.sdp518.domain;

import org.neo4j.ogm.annotation.Relationship;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class AnnotatableEntity extends Entity {

    @Relationship(type = "ANNOTATES", direction = Relationship.INCOMING)
    Set<Annotation> annotations = new HashSet<>();

    public AnnotatableEntity() {
        super();
    }

    public AnnotatableEntity(String fullyQualifiedName, String name) {
        super(fullyQualifiedName, name);
    }

    public void addAllAnnotations(Collection<Annotation> annotations) {
        this.annotations.addAll(annotations);
    }
}
