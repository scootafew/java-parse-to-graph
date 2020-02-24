package com.york.sdp518.exception;

public class BuildClasspathException extends JavaParseToGraphException {

    public BuildClasspathException(String message, Throwable cause) {
        super(message, cause, ErrorCode.BUILD_CLASSPATH);
    }

    public BuildClasspathException(String message) {
        super(message, ErrorCode.BUILD_CLASSPATH);
    }

    public BuildClasspathException(Throwable cause) {
        super(cause, ErrorCode.BUILD_CLASSPATH);
    }
}
