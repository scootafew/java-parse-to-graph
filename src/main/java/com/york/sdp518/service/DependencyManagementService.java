package com.york.sdp518.service;

import com.york.sdp518.domain.Artifact;
import com.york.sdp518.exception.DependencyManagementServiceException;

import java.io.File;
import java.nio.file.Path;
import java.util.Set;

public interface DependencyManagementService<T> {

    public String getLatestVersion(String groupId, String artifactId) throws DependencyManagementServiceException;

    public boolean isPublishedArtifact(String groupId, String artifactId) throws DependencyManagementServiceException;

//    public String getProjectVersion(File dependencyFile) throws DependencyManagementServiceException;
//
//    public void setVersion(File dependencyFile, String version) throws DependencyManagementServiceException;

    public void build(File dependencyFile, boolean runTests) throws DependencyManagementServiceException;

    public Set<T> getDependencies(File dependencyFile) throws DependencyManagementServiceException;

    public Path getSources(Artifact artifact) throws DependencyManagementServiceException;

    public void buildClasspath(File dependencyFile) throws DependencyManagementServiceException;
}
