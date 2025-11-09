package com.snipper.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("testuser", "test@example.com", "hashedpassword");
    }

    @Test
    void testUserCreation() {
        assertNotNull(user);
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("hashedpassword", user.getPassword());
        assertTrue(user.getIsActive());
        assertNotNull(user.getSnippets());
        assertTrue(user.getSnippets().isEmpty());
    }

    @Test
    void testUserDefaultValues() {
        User newUser = new User();
        assertNull(newUser.getUsername());
        assertNull(newUser.getEmail());
        assertNull(newUser.getPassword());
        assertTrue(newUser.getIsActive());
        assertNotNull(newUser.getSnippets());
    }

    @Test
    void testUserSettersAndGetters() {
        user.setFullName("Test User");
        user.setBio("This is a test user");
        user.setIsActive(false);

        assertEquals("Test User", user.getFullName());
        assertEquals("This is a test user", user.getBio());
        assertFalse(user.getIsActive());
    }

    @Test
    void testUserToString() {
        String userString = user.toString();
        assertTrue(userString.contains("testuser"));
        assertTrue(userString.contains("test@example.com"));
        assertTrue(userString.contains("User{"));
    }
}