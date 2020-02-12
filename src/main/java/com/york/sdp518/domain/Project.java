package com.york.sdp518.domain;

import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public class Project {

    private String fullyQualifiedName;
    private String name;

    public Project() {

    }

}
