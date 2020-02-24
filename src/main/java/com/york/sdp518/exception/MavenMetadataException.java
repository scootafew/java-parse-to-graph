package com.york.sdp518.exception;

public class MavenMetadataException extends JavaParseToGraphException {

    public MavenMetadataException(String message, Throwable cause) {
        super(message, cause, ErrorCode.MAVEN_METADATA);
    }

    public MavenMetadataException(String message) {
        super(message, ErrorCode.MAVEN_METADATA);
    }

    public MavenMetadataException(Throwable cause) {
        super(cause, ErrorCode.MAVEN_METADATA);
    }
}
