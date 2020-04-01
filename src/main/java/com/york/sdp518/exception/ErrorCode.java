package com.york.sdp518.exception;

public enum ErrorCode {
    DEFAULT(-1, "Error processing"),
    VCS(10, "Error cloning from VCS"),
    MAVEN_METADATA(10, "Could not retrieve maven metadata"),
    BUILD_CLASSPATH(11, "Could not build classpath"),
    MAVEN_INVOCATION(12, "Error invoking maven plugin");

    private final int code;
    private final String description;

    ErrorCode(int code, String description){
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return code + ": " + description;
    }
}
