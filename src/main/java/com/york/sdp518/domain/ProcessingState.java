package com.york.sdp518.domain;

public enum ProcessingState {
    NOT_PROCESSED("not_processed"),
    FAILED("failed"),
    COMPLETED("completed"),
    IN_PROGRESS("in_progress");

    private final String state;

    ProcessingState(String state){
        this.state = state;
    }

    public String getState() {
        return state;
    }

    @Override
    public String toString() {
        return state;
    }
}
