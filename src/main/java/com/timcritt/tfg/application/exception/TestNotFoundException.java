package com.timcritt.tfg.application.exception;

public class TestNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final Long testId;

    public TestNotFoundException(Long testId) {
        super("Test not found with id: " + testId);
        this.testId = testId;
    }

    public TestNotFoundException(Long testId, String message) {
        super(message);
        this.testId = testId;
    }

    public Long getTestId() {
        return testId;
    }
}
