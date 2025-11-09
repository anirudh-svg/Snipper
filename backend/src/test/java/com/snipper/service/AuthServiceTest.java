package com.snipper.service;

import com.snipper.dto.auth.AuthResponse;
import com.snipper.dto.auth.LoginRequest;
import com.snipper.dto.auth.RegisterRequest;
import com.snipper.model.User;
import com.snipper.repository.UserRepository;
import com.snipper.security.CustomUserPrincipal;
import com.snipper.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedpassword");
        testUser.setFullName("Test User");
        testUser.setIsActive(true);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFullName("Test User");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");
    }

    @Test
    void register_Success() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedpassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtUtil.generateToken("testuser")).thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals(1L, response.getUser().getId());
        assertEquals("testuser", response.getUser().getUsername());
        assertEquals("test@example.com", response.getUser().getEmail());
        assertEquals("Bearer", response.getTokenType());

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(jwtUtil, times(2)).generateToken("testuser"); // Called twice: access token + refresh token
    }

    @Test
    void register_UsernameAlreadyExists() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.register(registerRequest));
        
        assertEquals("Username is already taken", exception.getMessage());
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_EmailAlreadyExists() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.register(registerRequest));
        
        assertEquals("Email is already registered", exception.getMessage());
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(com.snipper.security.CustomUserPrincipal.create(testUser));
        when(userRepository.findByUsernameAndIsActiveTrue("testuser"))
            .thenReturn(Optional.of(testUser));
        when(jwtUtil.generateToken("testuser")).thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals(1L, response.getUser().getId());
        assertEquals("testuser", response.getUser().getUsername());
        assertEquals("test@example.com", response.getUser().getEmail());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUsernameAndIsActiveTrue("testuser");
        verify(jwtUtil, times(2)).generateToken("testuser"); // Called twice: access token + refresh token
    }

    @Test
    void login_InvalidCredentials() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.login(loginRequest));
        
        assertEquals("Invalid username or password", exception.getMessage());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByUsernameAndIsActiveTrue(anyString());
    }

    @Test
    void login_UserNotFound() {
        // Arrange
        CustomUserPrincipal principal = new CustomUserPrincipal(1L, "testuser", "test@example.com", "password", true, Collections.emptyList());
        when(authentication.getPrincipal()).thenReturn(principal);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(userRepository.findByUsernameAndIsActiveTrue("testuser"))
            .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.login(loginRequest));
        
        assertEquals("User not found or inactive", exception.getMessage());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUsernameAndIsActiveTrue("testuser");
    }

    @Test
    void refreshToken_Success() {
        // Arrange
        String oldToken = "old-jwt-token";
        String newToken = "new-jwt-token";
        
        when(jwtUtil.validateToken(oldToken)).thenReturn(true);
        when(jwtUtil.extractUsername(oldToken)).thenReturn("testuser");
        when(userRepository.findByUsernameAndIsActiveTrue("testuser"))
            .thenReturn(Optional.of(testUser));
        when(jwtUtil.generateToken("testuser")).thenReturn(newToken);

        // Act
        AuthResponse response = authService.refreshToken(oldToken);

        // Assert
        assertNotNull(response);
        assertEquals(newToken, response.getToken());
        assertEquals(1L, response.getUser().getId());
        assertEquals("testuser", response.getUser().getUsername());
        assertEquals("test@example.com", response.getUser().getEmail());

        verify(jwtUtil).validateToken(oldToken);
        verify(jwtUtil).extractUsername(oldToken);
        verify(userRepository).findByUsernameAndIsActiveTrue("testuser");
        verify(jwtUtil, times(2)).generateToken("testuser"); // Called twice: access token + refresh token
    }

    @Test
    void refreshToken_InvalidToken() {
        // Arrange
        String invalidToken = "invalid-token";
        when(jwtUtil.validateToken(invalidToken)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.refreshToken(invalidToken));
        
        assertEquals("Failed to refresh token: Invalid token", exception.getMessage());
        verify(jwtUtil).validateToken(invalidToken);
        verify(jwtUtil, never()).extractUsername(anyString());
    }

    @Test
    void refreshToken_UserNotFound() {
        // Arrange
        String token = "valid-token";
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.extractUsername(token)).thenReturn("testuser");
        when(userRepository.findByUsernameAndIsActiveTrue("testuser"))
            .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.refreshToken(token));
        
        assertEquals("Failed to refresh token: User not found or inactive", exception.getMessage());
        verify(jwtUtil).validateToken(token);
        verify(jwtUtil).extractUsername(token);
        verify(userRepository).findByUsernameAndIsActiveTrue("testuser");
    }

    @Test
    void isUserActive_UserExists() {
        // Arrange
        when(userRepository.findByUsernameAndIsActiveTrue("testuser"))
            .thenReturn(Optional.of(testUser));

        // Act
        boolean result = authService.isUserActive("testuser");

        // Assert
        assertTrue(result);
        verify(userRepository).findByUsernameAndIsActiveTrue("testuser");
    }

    @Test
    void isUserActive_UserNotExists() {
        // Arrange
        when(userRepository.findByUsernameAndIsActiveTrue("testuser"))
            .thenReturn(Optional.empty());

        // Act
        boolean result = authService.isUserActive("testuser");

        // Assert
        assertFalse(result);
        verify(userRepository).findByUsernameAndIsActiveTrue("testuser");
    }
}