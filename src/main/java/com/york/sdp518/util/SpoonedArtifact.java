package com.york.sdp518.util;

import com.york.sdp518.domain.Artifact;
import com.york.sdp518.exception.PomFileException;
import com.york.sdp518.service.impl.MavenDependencyManagementService;

import java.nio.file.Path;

public class SpoonedArtifact extends SpoonedMavenProject {

    private Artifact artifact;

    public SpoonedArtifact(Path projectDirectory, MavenDependencyManagementService dms) throws PomFileException {
        super(projectDirectory, dms);
        this.artifact = getRootPom().asArtifact();
    }

    public SpoonedArtifact(Path projectDirectory, Artifact artifact,
                           MavenDependencyManagementService dms) throws PomFileException {
        super(projectDirectory, dms);
        this.artifact = artifact;
    }

    public Artifact getArtifact() {
        return artifact;
    }

    public void printArtifact() {
        OutputUtils.printArtifact(artifact);
    }

}
