package com.york.sdp518.domain;

import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public class Artifact extends Entity {

    public Artifact() {

    }

    public Artifact(String fullyQualifiedName, String name) {
        this();
        setFullyQualifiedName(fullyQualifiedName);
        setName(name);
    }
}
