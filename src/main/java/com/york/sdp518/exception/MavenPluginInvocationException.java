package com.york.sdp518.exception;

public class MavenPluginInvocationException extends DependencyManagementServiceException {

    public MavenPluginInvocationException(String message, Throwable cause) {
        super(message, cause, ErrorCode.MAVEN_INVOCATION);
    }

    public MavenPluginInvocationException(String message) {
        super(message, ErrorCode.MAVEN_INVOCATION);
    }

    public MavenPluginInvocationException(Throwable cause) {
        super(cause, ErrorCode.MAVEN_INVOCATION);
    }
}
