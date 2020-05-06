package com.york.sdp518.service;

import com.york.sdp518.domain.Artifact;
import com.york.sdp518.exception.DependencyManagementServiceException;

import java.io.File;
import java.nio.file.Path;
import java.util.Set;

public interface DependencyManagementService<T> {

    String getLatestVersion(String groupId, String artifactId) throws DependencyManagementServiceException;

    boolean isPublishedArtifact(String groupId, String artifactId) throws DependencyManagementServiceException;

    void build(File dependencyFile, boolean runTests) throws DependencyManagementServiceException;

    Set<T> getDependencies(File dependencyFile) throws DependencyManagementServiceException;

    Path getSources(Artifact artifact) throws DependencyManagementServiceException;

    void buildClasspath(File dependencyFile) throws DependencyManagementServiceException;
}
