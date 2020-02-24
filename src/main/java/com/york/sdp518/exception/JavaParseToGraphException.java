package com.york.sdp518.exception;

public abstract class JavaParseToGraphException extends Exception {

    private static final long serialVersionUID = 7718828512143293558L;

    private final ErrorCode code;

    public JavaParseToGraphException(ErrorCode code) {
        super();
        this.code = code;
    }

    public JavaParseToGraphException(String message, Throwable cause, ErrorCode code) {
        super(message, cause);
        this.code = code;
    }

    public JavaParseToGraphException(String message, ErrorCode code) {
        super(message);
        this.code = code;
    }

    public JavaParseToGraphException(Throwable cause, ErrorCode code) {
        super(cause);
        this.code = code;
    }

    public ErrorCode getCode() {
        return this.code;
    }
}
