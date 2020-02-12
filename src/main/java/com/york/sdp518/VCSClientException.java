package com.york.sdp518;

public class VCSClientException extends Exception {

    public VCSClientException() {}
    public VCSClientException(String msg) { super(msg); }
    public VCSClientException(Throwable cause) { super(cause); }
    public VCSClientException(String msg, Throwable cause) { super(msg, cause); }
}
