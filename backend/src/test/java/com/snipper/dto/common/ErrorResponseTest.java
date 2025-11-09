package com.snipper.dto.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void defaultConstructor_ShouldSetTimestamp() {
        // When
        ErrorResponse errorResponse = new ErrorResponse();

        // Then
        assertNotNull(errorResponse.getTimestamp());
        assertTrue(errorResponse.getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(errorResponse.getTimestamp().isAfter(LocalDateTime.now().minusSeconds(1)));
    }

    @Test
    void constructorWithBasicFields_ShouldSetAllFields() {
        // Given
        int status = 400;
        String error = "Bad Request";
        String message = "Invalid input";
        String path = "/api/test";

        // When
        ErrorResponse errorResponse = new ErrorResponse(status, error, message, path);

        // Then
        assertNotNull(errorResponse.getTimestamp());
        assertEquals(status, errorResponse.getStatus());
        assertEquals(error, errorResponse.getError());
        assertEquals(message, errorResponse.getMessage());
        assertEquals(path, errorResponse.getPath());
        assertNull(errorResponse.getErrors());
    }

    @Test
    void constructorWithErrors_ShouldSetAllFields() {
        // Given
        int status = 400;
        String error = "Validation Failed";
        String message = "Input validation failed";
        String path = "/api/test";
        Map<String, String> errors = new HashMap<>();
        errors.put("username", "Username is required");
        errors.put("email", "Email is invalid");

        // When
        ErrorResponse errorResponse = new ErrorResponse(status, error, message, path, errors);

        // Then
        assertNotNull(errorResponse.getTimestamp());
        assertEquals(status, errorResponse.getStatus());
        assertEquals(error, errorResponse.getError());
        assertEquals(message, errorResponse.getMessage());
        assertEquals(path, errorResponse.getPath());
        assertEquals(errors, errorResponse.getErrors());
        assertEquals("Username is required", errorResponse.getErrors().get("username"));
        assertEquals("Email is invalid", errorResponse.getErrors().get("email"));
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        // Given
        ErrorResponse errorResponse = new ErrorResponse();
        LocalDateTime timestamp = LocalDateTime.now();
        int status = 404;
        String error = "Not Found";
        String message = "Resource not found";
        String path = "/api/snippets/123";
        Map<String, String> errors = new HashMap<>();
        errors.put("id", "Invalid ID format");

        // When
        errorResponse.setTimestamp(timestamp);
        errorResponse.setStatus(status);
        errorResponse.setError(error);
        errorResponse.setMessage(message);
        errorResponse.setPath(path);
        errorResponse.setErrors(errors);

        // Then
        assertEquals(timestamp, errorResponse.getTimestamp());
        assertEquals(status, errorResponse.getStatus());
        assertEquals(error, errorResponse.getError());
        assertEquals(message, errorResponse.getMessage());
        assertEquals(path, errorResponse.getPath());
        assertEquals(errors, errorResponse.getErrors());
    }

    @Test
    void jsonSerialization_ShouldExcludeNullFields() throws Exception {
        // Given
        ErrorResponse errorResponse = new ErrorResponse(400, "Bad Request", "Invalid input", "/api/test");

        // When
        String json = objectMapper.writeValueAsString(errorResponse);

        // Then
        assertFalse(json.contains("\"errors\""), "JSON should not contain null errors field");
        assertTrue(json.contains("\"status\":400"));
        assertTrue(json.contains("\"error\":\"Bad Request\""));
        assertTrue(json.contains("\"message\":\"Invalid input\""));
        assertTrue(json.contains("\"path\":\"/api/test\""));
        assertTrue(json.contains("\"timestamp\""));
    }

    @Test
    void jsonSerialization_ShouldIncludeErrorsWhenPresent() throws Exception {
        // Given
        Map<String, String> errors = new HashMap<>();
        errors.put("username", "Username is required");
        ErrorResponse errorResponse = new ErrorResponse(400, "Validation Failed", "Input validation failed", "/api/test", errors);

        // When
        String json = objectMapper.writeValueAsString(errorResponse);

        // Then
        assertTrue(json.contains("\"errors\""));
        assertTrue(json.contains("\"username\":\"Username is required\""));
    }

    @Test
    void jsonDeserialization_ShouldWorkCorrectly() throws Exception {
        // Given
        String json = """
            {
                "timestamp": "2023-10-27T10:30:00",
                "status": 400,
                "error": "Bad Request",
                "message": "Invalid input",
                "path": "/api/test",
                "errors": {
                    "username": "Username is required"
                }
            }
            """;

        // When
        ErrorResponse errorResponse = objectMapper.readValue(json, ErrorResponse.class);

        // Then
        assertNotNull(errorResponse.getTimestamp());
        assertEquals(400, errorResponse.getStatus());
        assertEquals("Bad Request", errorResponse.getError());
        assertEquals("Invalid input", errorResponse.getMessage());
        assertEquals("/api/test", errorResponse.getPath());
        assertNotNull(errorResponse.getErrors());
        assertEquals("Username is required", errorResponse.getErrors().get("username"));
    }
}