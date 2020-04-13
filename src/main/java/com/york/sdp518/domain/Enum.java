package com.york.sdp518.domain;

import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public class Enum extends Type {

    public Enum() {
        super();
    }

    public Enum(String fullyQualifiedName, String name) {
        super(fullyQualifiedName, name);
    }
}
