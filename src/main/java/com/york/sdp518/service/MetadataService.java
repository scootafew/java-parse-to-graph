package com.york.sdp518.service;

import com.york.sdp518.exception.MavenMetadataException;

public interface MetadataService {

    String getLatestVersion(String groupId, String artifactId) throws MavenMetadataException;
}
