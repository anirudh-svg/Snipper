package com.snipper.validation;

import com.snipper.dto.auth.RegisterRequest;
import com.snipper.dto.snippet.CreateSnippetRequest;
import com.snipper.dto.user.UpdateProfileRequest;
import com.snipper.model.VisibilityType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for validation annotations on DTOs
 */
class ValidationIntegrationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void registerRequest_WithValidData_ShouldPassValidation() {
        // Given
        RegisterRequest request = new RegisterRequest("testuser", "test@example.com", "password123", "Test User");

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void registerRequest_WithInvalidData_ShouldFailValidation() {
        // Given
        RegisterRequest request = new RegisterRequest("ab", "invalid-email", "123", null);

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertEquals(3, violations.size());
        
        // Check specific validation messages
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Username must be between 3 and 50 characters")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Email should be valid")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Password must be between 6 and 100 characters")));
    }

    @Test
    void registerRequest_WithBlankFields_ShouldFailValidation() {
        // Given
        RegisterRequest request = new RegisterRequest("", "", "", "");

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Username is required")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Email is required")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Password is required")));
    }

    @Test
    void createSnippetRequest_WithValidData_ShouldPassValidation() {
        // Given
        CreateSnippetRequest request = new CreateSnippetRequest(
            "Test Snippet",
            "A test snippet",
            "console.log('Hello World');",
            "javascript",
            "test,javascript",
            VisibilityType.PUBLIC
        );

        // When
        Set<ConstraintViolation<CreateSnippetRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void createSnippetRequest_WithInvalidData_ShouldFailValidation() {
        // Given
        CreateSnippetRequest request = new CreateSnippetRequest();
        request.setTitle(""); // Blank title
        request.setContent(""); // Blank content
        request.setLanguage(""); // Blank language
        request.setVisibility(null); // Null visibility

        // When
        Set<ConstraintViolation<CreateSnippetRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Title is required")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Content is required")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Language is required")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Visibility is required")));
    }

    @Test
    void createSnippetRequest_WithTooLongFields_ShouldFailValidation() {
        // Given
        String longTitle = "a".repeat(201); // Exceeds 200 character limit
        String longDescription = "a".repeat(1001); // Exceeds 1000 character limit
        String longLanguage = "a".repeat(51); // Exceeds 50 character limit
        String longTags = "a".repeat(501); // Exceeds 500 character limit

        CreateSnippetRequest request = new CreateSnippetRequest(
            longTitle,
            longDescription,
            "console.log('Hello');",
            longLanguage,
            longTags,
            VisibilityType.PUBLIC
        );

        // When
        Set<ConstraintViolation<CreateSnippetRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Title must not exceed 200 characters")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Description must not exceed 1000 characters")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Language must not exceed 50 characters")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Tags must not exceed 500 characters")));
    }

    @Test
    void updateProfileRequest_WithValidData_ShouldPassValidation() {
        // Given
        UpdateProfileRequest request = new UpdateProfileRequest("newuser", "new@example.com", "New User", "Bio text");

        // When
        Set<ConstraintViolation<UpdateProfileRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void updateProfileRequest_WithInvalidEmail_ShouldFailValidation() {
        // Given
        UpdateProfileRequest request = new UpdateProfileRequest("user", "invalid-email", "User", "Bio");

        // When
        Set<ConstraintViolation<UpdateProfileRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Email should be valid")));
    }

    @Test
    void updateProfileRequest_WithTooShortUsername_ShouldFailValidation() {
        // Given
        UpdateProfileRequest request = new UpdateProfileRequest("ab", "user@example.com", "User", "Bio");

        // When
        Set<ConstraintViolation<UpdateProfileRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Username must be between 3 and 50 characters")));
    }

    @Test
    void updateProfileRequest_WithTooLongFields_ShouldFailValidation() {
        // Given
        String longUsername = "a".repeat(51); // Exceeds 50 character limit
        String longEmail = "a".repeat(95) + "@example.com"; // Exceeds 100 character limit
        String longFullName = "a".repeat(101); // Exceeds 100 character limit
        String longBio = "a".repeat(501); // Exceeds 500 character limit

        UpdateProfileRequest request = new UpdateProfileRequest(longUsername, longEmail, longFullName, longBio);

        // When
        Set<ConstraintViolation<UpdateProfileRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Username must be between 3 and 50 characters")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Email must not exceed 100 characters")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Full name must not exceed 100 characters")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Bio must not exceed 500 characters")));
    }

    @Test
    void updateProfileRequest_WithNullFields_ShouldPassValidation() {
        // Given - UpdateProfileRequest allows null fields for partial updates
        UpdateProfileRequest request = new UpdateProfileRequest(null, null, null, null);

        // When
        Set<ConstraintViolation<UpdateProfileRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty(), "UpdateProfileRequest should allow null fields for partial updates");
    }
}