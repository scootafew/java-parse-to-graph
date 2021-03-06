package com.york.sdp518.domain;

import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public class Artifact extends ProcessableEntity {

    String groupId;
    String artifactId;
    String version;

    public Artifact() {
        super();
    }

    public Artifact(String fullyQualifiedName) {
        this(fullyQualifiedName, getArtifactIdFromArtifact(fullyQualifiedName));
    }

    public Artifact(String fullyQualifiedName, String name) {
        super(fullyQualifiedName, name);
        this.groupId = getGroupIdFromArtifact(fullyQualifiedName);
        this.artifactId = name;
        this.version = getVersionFromArtifact(fullyQualifiedName);
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    private static String getGroupIdFromArtifact(String artifact) {
        return artifact.split(":")[0];
    }

    private static String getArtifactIdFromArtifact(String artifact) {
        return artifact.split(":")[1];
    }

    private static String getVersionFromArtifact(String artifact) {
        return artifact.split(":")[2];
    }
}
