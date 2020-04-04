package com.york.sdp518.service.impl;

import com.york.sdp518.domain.Artifact;
import com.york.sdp518.exception.DependencyManagementServiceException;
import com.york.sdp518.service.DependencyManagementService;
import org.apache.maven.model.Dependency;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.util.Set;

@Service
public class MavenDependencyManagementService implements DependencyManagementService<Dependency> {

    private MavenMetadataService mavenMetadataService;
    private MavenPluginService mavenPluginService;

    @Autowired
    public MavenDependencyManagementService(MavenMetadataService mavenMetadataService, MavenPluginService mavenPluginService) {
        this.mavenMetadataService = mavenMetadataService;
        this.mavenPluginService = mavenPluginService;
    }

    @Override
    public String getLatestVersion(String groupId, String artifactId) throws DependencyManagementServiceException {
        return mavenMetadataService.getLatestVersion(groupId, artifactId);
    }

    @Override
    public boolean isPublishedArtifact(String groupId, String artifactId) throws DependencyManagementServiceException {
        return mavenMetadataService.isPublishedArtifact(groupId, artifactId);
    }

    @Override
    public void build(File dependencyFile, boolean runTests) throws DependencyManagementServiceException {
        mavenPluginService.cleanInstall(dependencyFile, runTests);
    }

    @Override
    public Set<Dependency> getDependencies(File dependencyFile) throws DependencyManagementServiceException {
        return mavenPluginService.getDependencies(dependencyFile);
    }

    @Override
    public Path getSources(Artifact artifact) throws DependencyManagementServiceException {
        return mavenPluginService.getSources(artifact);
    }

    @Override
    public void buildClasspath(File dependencyFile) throws DependencyManagementServiceException {
        mavenPluginService.buildClasspath(dependencyFile);
    }
}
