package com.snipper.exception;

import com.snipper.dto.common.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    private WebRequest webRequest;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        webRequest = mock(WebRequest.class);
        objectMapper = new ObjectMapper();
        when(webRequest.getDescription(false)).thenReturn("uri=/api/test");
    }

    @Test
    void handleValidationExceptions_ShouldReturnBadRequest() {
        // Given
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "testObject");
        bindingResult.addError(new FieldError("testObject", "username", "Username is required"));
        bindingResult.addError(new FieldError("testObject", "email", "Email is invalid"));
        
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidationExceptions(exception, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.getStatus());
        assertEquals("Validation Failed", body.getError());
        assertEquals("Input validation failed", body.getMessage());
        assertEquals("/api/test", body.getPath());
        
        Map<String, String> errors = body.getErrors();
        assertEquals("Username is required", errors.get("username"));
        assertEquals("Email is invalid", errors.get("email"));
    }

    @Test
    void handleConstraintViolationException_ShouldReturnBadRequest() {
        // Given
        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(violation.getPropertyPath()).thenReturn(path);
        when(path.toString()).thenReturn("email");
        when(violation.getMessage()).thenReturn("Email must be valid");
        
        Set<ConstraintViolation<Object>> violations = Set.of(violation);
        ConstraintViolationException exception = new ConstraintViolationException(violations);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleConstraintViolationException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.getStatus());
        assertEquals("Constraint Violation", body.getError());
        assertEquals("Validation constraint violated", body.getMessage());
    }

    @Test
    void handleMissingServletRequestParameter_ShouldReturnBadRequest() {
        // Given
        MissingServletRequestParameterException exception = 
            new MissingServletRequestParameterException("page", "int");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleMissingServletRequestParameter(exception, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.getStatus());
        assertEquals("Missing Parameter", body.getError());
        assertEquals("Required parameter 'page' is missing", body.getMessage());
    }

    @Test
    void handleMethodArgumentTypeMismatch_ShouldReturnBadRequest() {
        // Given
        MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
        when(exception.getName()).thenReturn("id");
        when(exception.getRequiredType()).thenReturn((Class) Long.class);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleMethodArgumentTypeMismatch(exception, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.getStatus());
        assertEquals("Type Mismatch", body.getError());
        assertEquals("Parameter 'id' should be of type Long", body.getMessage());
    }

    @Test
    void handleHttpMessageNotReadable_ShouldReturnBadRequest() {
        // Given
        HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleHttpMessageNotReadable(exception, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.getStatus());
        assertEquals("Malformed JSON", body.getError());
        assertEquals("Request body contains invalid JSON", body.getMessage());
    }

    @Test
    void handleHttpRequestMethodNotSupported_ShouldReturnMethodNotAllowed() {
        // Given
        HttpRequestMethodNotSupportedException exception = 
            new HttpRequestMethodNotSupportedException("POST");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleHttpRequestMethodNotSupported(exception, webRequest);

        // Then
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(405, body.getStatus());
        assertEquals("Method Not Allowed", body.getError());
        assertEquals("HTTP method 'POST' is not supported for this endpoint", body.getMessage());
    }

    @Test
    void handleAuthenticationException_ShouldReturnUnauthorized() {
        // Given
        BadCredentialsException exception = new BadCredentialsException("Invalid credentials");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleAuthenticationException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(401, body.getStatus());
        assertEquals("Authentication Failed", body.getError());
        assertEquals("Invalid credentials or authentication failed", body.getMessage());
    }

    @Test
    void handleAccessDeniedException_ShouldReturnForbidden() {
        // Given
        AccessDeniedException exception = new AccessDeniedException("Access denied");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleAccessDeniedException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(403, body.getStatus());
        assertEquals("Access Denied", body.getError());
        assertEquals("You don't have permission to access this resource", body.getMessage());
    }

    @Test
    void handleResourceNotFoundException_ShouldReturnNotFound() {
        // Given
        ResourceNotFoundException exception = new ResourceNotFoundException("Snippet not found");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleResourceNotFoundException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(404, body.getStatus());
        assertEquals("Resource Not Found", body.getError());
        assertEquals("Snippet not found", body.getMessage());
    }

    @Test
    void handleUnauthorizedException_ShouldReturnForbidden() {
        // Given
        UnauthorizedException exception = new UnauthorizedException("You cannot access this snippet");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleUnauthorizedException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(403, body.getStatus());
        assertEquals("Access Denied", body.getError());
        assertEquals("You cannot access this snippet", body.getMessage());
    }

    @Test
    void handleValidationException_ShouldReturnBadRequest() {
        // Given
        ValidationException exception = new ValidationException("Custom validation failed");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidationException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.getStatus());
        assertEquals("Validation Error", body.getError());
        assertEquals("Custom validation failed", body.getMessage());
    }

    @Test
    void handleDuplicateResourceException_ShouldReturnConflict() {
        // Given
        DuplicateResourceException exception = new DuplicateResourceException("Username already exists");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleDuplicateResourceException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(409, body.getStatus());
        assertEquals("Resource Already Exists", body.getError());
        assertEquals("Username already exists", body.getMessage());
    }

    @Test
    void handleInvalidTokenException_ShouldReturnUnauthorized() {
        // Given
        InvalidTokenException exception = new InvalidTokenException("JWT token is expired");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleInvalidTokenException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(401, body.getStatus());
        assertEquals("Invalid Token", body.getError());
        assertEquals("JWT token is expired", body.getMessage());
    }

    @Test
    void handleIllegalArgumentException_ShouldReturnBadRequest() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument provided");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalArgumentException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.getStatus());
        assertEquals("Invalid Argument", body.getError());
        assertEquals("Invalid argument provided", body.getMessage());
    }

    @Test
    void handleRuntimeException_ShouldReturnInternalServerError() {
        // Given
        RuntimeException exception = new RuntimeException("Unexpected runtime error");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleRuntimeException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(500, body.getStatus());
        assertEquals("Internal Server Error", body.getError());
        assertEquals("An unexpected error occurred", body.getMessage());
    }

    @Test
    void handleGlobalException_ShouldReturnInternalServerError() {
        // Given
        Exception exception = new Exception("Unexpected error");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGlobalException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(500, body.getStatus());
        assertEquals("Internal Server Error", body.getError());
        assertEquals("An unexpected error occurred", body.getMessage());
    }

    @Test
    void allExceptionHandlers_ShouldIncludeTimestampAndPath() {
        // Given
        ResourceNotFoundException exception = new ResourceNotFoundException("Test exception");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleResourceNotFoundException(exception, webRequest);

        // Then
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertNotNull(body.getTimestamp());
        assertEquals("/api/test", body.getPath());
    }
}