package com.york.sdp518.domain;

import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@NodeEntity
public class Repository {

    @Id
    private String url;

    private String name;

    @Relationship(type = "DECLARES")
    private Set<Artifact> artifacts;

    public Repository() {
        artifacts = new HashSet<>();
    }

    public Repository(String url, String name) {
        this();
        this.url = url;
        this.name = name;
    }

    public void addAllArtifacts(Collection<Artifact> artifacts) {
        this.artifacts.addAll(artifacts);
    }

}
