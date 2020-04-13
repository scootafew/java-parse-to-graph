package com.york.sdp518.domain;

import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public class Annotation extends Type {

    public Annotation() {
        super();
    }

    public Annotation(String fullyQualifiedName, String name) {
        super(fullyQualifiedName, name);
    }
}
