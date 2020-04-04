package com.york.sdp518.exception;

public class DependencyManagementServiceException extends JavaParseToGraphException {

    public DependencyManagementServiceException(String message, Throwable cause, ErrorCode errorCode) {
        super(message, cause, errorCode);
    }

    public DependencyManagementServiceException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

    public DependencyManagementServiceException(Throwable cause, ErrorCode errorCode) {
        super(cause, errorCode);
    }
}
