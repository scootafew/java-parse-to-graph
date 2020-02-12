package com.york.sdp518.domain;

import org.neo4j.ogm.annotation.Id;

public abstract class Entity {

    @Id
    private String fullyQualifiedName;

    private String name;

    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    public void setFullyQualifiedName(String fullyQualifiedName) {
        this.fullyQualifiedName = fullyQualifiedName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
