package com.york.sdp518.service;

import java.util.Optional;

public interface MetadataService {

    Optional<String> getLatestVersion(String groupId, String artifactId);
}
