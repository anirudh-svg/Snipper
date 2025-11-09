package com.snipper.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "testSecretKeyForJwtTokenGenerationAndValidation");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);
    }

    @Test
    void testGenerateToken() {
        String username = "testuser";
        String token = jwtUtil.generateToken(username);
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testExtractUsername() {
        String username = "testuser";
        String token = jwtUtil.generateToken(username);
        
        String extractedUsername = jwtUtil.extractUsername(token);
        
        assertEquals(username, extractedUsername);
    }

    @Test
    void testValidateToken() {
        String username = "testuser";
        String token = jwtUtil.generateToken(username);
        
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void testValidateTokenWithUserDetails() {
        String username = "testuser";
        UserDetails userDetails = new User(username, "password", new ArrayList<>());
        String token = jwtUtil.generateToken(userDetails);
        
        assertTrue(jwtUtil.validateToken(token, userDetails));
    }

    @Test
    void testInvalidToken() {
        String invalidToken = "invalid.token.here";
        
        assertFalse(jwtUtil.validateToken(invalidToken));
    }

    @Test
    void testExtractExpiration() {
        String username = "testuser";
        String token = jwtUtil.generateToken(username);
        
        assertNotNull(jwtUtil.extractExpiration(token));
    }
}