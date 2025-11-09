package com.snipper.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomExceptionsTest {

    @Test
    void resourceNotFoundException_ShouldCreateWithMessage() {
        // Given
        String message = "Resource not found";

        // When
        ResourceNotFoundException exception = new ResourceNotFoundException(message);

        // Then
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void resourceNotFoundException_ShouldCreateWithMessageAndCause() {
        // Given
        String message = "Resource not found";
        Throwable cause = new RuntimeException("Database error");

        // When
        ResourceNotFoundException exception = new ResourceNotFoundException(message, cause);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void unauthorizedException_ShouldCreateWithMessage() {
        // Given
        String message = "Access denied";

        // When
        UnauthorizedException exception = new UnauthorizedException(message);

        // Then
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void unauthorizedException_ShouldCreateWithMessageAndCause() {
        // Given
        String message = "Access denied";
        Throwable cause = new RuntimeException("Security error");

        // When
        UnauthorizedException exception = new UnauthorizedException(message, cause);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void validationException_ShouldCreateWithMessage() {
        // Given
        String message = "Validation failed";

        // When
        ValidationException exception = new ValidationException(message);

        // Then
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void validationException_ShouldCreateWithMessageAndCause() {
        // Given
        String message = "Validation failed";
        Throwable cause = new RuntimeException("Validation error");

        // When
        ValidationException exception = new ValidationException(message, cause);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void duplicateResourceException_ShouldCreateWithMessage() {
        // Given
        String message = "Resource already exists";

        // When
        DuplicateResourceException exception = new DuplicateResourceException(message);

        // Then
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void duplicateResourceException_ShouldCreateWithMessageAndCause() {
        // Given
        String message = "Resource already exists";
        Throwable cause = new RuntimeException("Database constraint violation");

        // When
        DuplicateResourceException exception = new DuplicateResourceException(message, cause);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void invalidTokenException_ShouldCreateWithMessage() {
        // Given
        String message = "Token is invalid";

        // When
        InvalidTokenException exception = new InvalidTokenException(message);

        // Then
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void invalidTokenException_ShouldCreateWithMessageAndCause() {
        // Given
        String message = "Token is invalid";
        Throwable cause = new RuntimeException("JWT parsing error");

        // When
        InvalidTokenException exception = new InvalidTokenException(message, cause);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void allCustomExceptions_ShouldBeRuntimeExceptions() {
        // Test that all custom exceptions extend RuntimeException
        assertTrue(RuntimeException.class.isAssignableFrom(ResourceNotFoundException.class));
        assertTrue(RuntimeException.class.isAssignableFrom(UnauthorizedException.class));
        assertTrue(RuntimeException.class.isAssignableFrom(ValidationException.class));
        assertTrue(RuntimeException.class.isAssignableFrom(DuplicateResourceException.class));
        assertTrue(RuntimeException.class.isAssignableFrom(InvalidTokenException.class));
    }
}