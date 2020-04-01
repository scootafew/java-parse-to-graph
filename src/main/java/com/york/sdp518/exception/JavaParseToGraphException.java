package com.york.sdp518.exception;

public class JavaParseToGraphException extends Exception {

    private static final long serialVersionUID = 7718828512143293558L;

    private final ErrorCode code;

    JavaParseToGraphException(ErrorCode code) {
        super();
        this.code = code;
    }

    public JavaParseToGraphException(String message, Throwable cause) {
        super(message, cause);
        this.code = ErrorCode.DEFAULT;
    }

    public JavaParseToGraphException(String message) {
        super(message);
        this.code = ErrorCode.DEFAULT;
    }

    JavaParseToGraphException(String message, Throwable cause, ErrorCode code) {
        super(message, cause);
        this.code = code;
    }

    JavaParseToGraphException(String message, ErrorCode code) {
        super(message);
        this.code = code;
    }

    JavaParseToGraphException(Throwable cause, ErrorCode code) {
        super(cause);
        this.code = code;
    }

    public ErrorCode getCode() {
        return this.code;
    }
}
