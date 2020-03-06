package com.york.sdp518.exception;

// TODO Fix error codes
public class PomFileException extends JavaParseToGraphException {

    public PomFileException(String message, Throwable cause) {
        super(message, cause, ErrorCode.BUILD_CLASSPATH);
    }

    public PomFileException(String message) {
        super(message, ErrorCode.BUILD_CLASSPATH);
    }

    public PomFileException(Throwable cause) {
        super(cause, ErrorCode.BUILD_CLASSPATH);
    }
}
