package com.york.sdp518.exception;

public class VCSClientException extends JavaParseToGraphException {

    public VCSClientException(String msg) {
        super(msg, ErrorCode.VCS);
    }

    public VCSClientException(Throwable cause) {
        super(cause, ErrorCode.VCS);
    }

    public VCSClientException(String msg, Throwable cause) {
        super(msg, cause, ErrorCode.VCS);
    }
}
