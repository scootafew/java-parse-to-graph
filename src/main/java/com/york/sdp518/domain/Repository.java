package com.york.sdp518.domain;

import com.york.sdp518.util.Utils;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@NodeEntity
public class Repository extends ProcessableEntity {

    @Relationship(type = "DECLARES")
    Set<Artifact> artifacts = new HashSet<>();

    public Repository() {
        super();
    }

    public Repository(String url, String name) {
        super(url, name);
    }

    public Repository(String url) {
        super(url, Utils.repoNameFromURI(url));
    }

    public void addAllArtifacts(Collection<Artifact> artifacts) {
        this.artifacts.addAll(artifacts);
    }

    public void addArtifact(Artifact artifact) {
        this.artifacts.addAll(Collections.singleton(artifact));
    }

    public String getUrl() {
        return super.getFullyQualifiedName();
    }
}
