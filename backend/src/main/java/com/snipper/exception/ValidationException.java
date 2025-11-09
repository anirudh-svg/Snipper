package com.snipper.exception;

/**
 * Custom exception for validation errors that are not handled by standard Bean Validation
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}