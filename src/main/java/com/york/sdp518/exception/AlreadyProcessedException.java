package com.york.sdp518.exception;

// TODO Fix error codes
public class AlreadyProcessedException extends JavaParseToGraphException {

    public AlreadyProcessedException(String message, Throwable cause) {
        super(message, cause, ErrorCode.BUILD_CLASSPATH);
    }

    public AlreadyProcessedException(String message) {
        super(message, ErrorCode.BUILD_CLASSPATH);
    }

    public AlreadyProcessedException(Throwable cause) {
        super(cause, ErrorCode.BUILD_CLASSPATH);
    }
}
