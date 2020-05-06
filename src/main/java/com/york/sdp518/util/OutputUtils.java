package com.york.sdp518.util;

import com.york.sdp518.domain.Artifact;
import org.apache.maven.model.Dependency;

public final class OutputUtils {

    private OutputUtils() {

    }

    public static void printDependency(Dependency dep) {
        String fqn = String.join(":", dep.getGroupId(), dep.getArtifactId(), dep.getVersion());
        System.out.println("Found maven dependency: " + fqn);
    }

    public static void printArtifact(Artifact artifact) {
        System.out.println("Found maven artifact: " + artifact.getFullyQualifiedName());
    }
}
