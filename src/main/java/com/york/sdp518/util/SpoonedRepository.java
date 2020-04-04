package com.york.sdp518.util;

import com.york.sdp518.domain.Artifact;
import com.york.sdp518.exception.PomFileException;
import com.york.sdp518.service.impl.MavenDependencyManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

public class SpoonedRepository extends SpoonedMavenProject {

    private static final Logger logger = LoggerFactory.getLogger(SpoonedRepository.class);

    private String remoteUrl;
    private Set<Artifact> artifacts;

    public SpoonedRepository(Path projectDirectory, String remoteUrl,
                             MavenDependencyManagementService dms) throws PomFileException {
        super(projectDirectory, dms);
        this.remoteUrl = remoteUrl;
        this.artifacts = getAllModules().stream().map(PomModel::asArtifact).collect(Collectors.toSet());
    }

    public Set<Artifact> getArtifacts() {
        return artifacts;
    }

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public String getProjectName() {
        return Utils.repoNameFromURI(remoteUrl);
    }

    public void printArtifacts() {
        artifacts.forEach(OutputUtils::printArtifact);
    }
}
