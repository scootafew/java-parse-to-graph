package com.york.sdp518.util;

import com.york.sdp518.domain.Artifact;
import com.york.sdp518.exception.PomFileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class SpoonedArtifact extends SpoonedMavenProject {

    private static final Logger logger = LoggerFactory.getLogger(SpoonedArtifact.class);

    private Artifact artifact;

    public SpoonedArtifact(Path projectDirectory) throws PomFileException {
        super(projectDirectory);
        this.artifact = getRootPom().asArtifact();

        printArtifact(artifact);
        printDependencies();
    }

    public SpoonedArtifact(Path projectDirectory, Artifact artifact) throws PomFileException {
        super(projectDirectory);
        this.artifact = artifact;

        printArtifact(artifact);
        printDependencies();
    }

    public Artifact getArtifact() {
        return artifact;
    }
}
