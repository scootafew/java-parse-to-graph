package com.york.sdp518.exception;

public class PomFileException extends JavaParseToGraphException {

    public PomFileException(String message, Throwable cause) {
        super(message, cause, ErrorCode.POM_FILE);
    }

    public PomFileException(String message) {
        super(message, ErrorCode.POM_FILE);
    }

    public PomFileException(Throwable cause) {
        super(cause, ErrorCode.POM_FILE);
    }
}
