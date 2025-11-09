package com.snipper.service;

import com.snipper.dto.auth.AuthResponse;
import com.snipper.dto.auth.LoginRequest;
import com.snipper.dto.auth.RegisterRequest;
import com.snipper.model.User;
import com.snipper.repository.UserRepository;
import com.snipper.security.CustomUserPrincipal;
import com.snipper.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthService(UserRepository userRepository, 
                      PasswordEncoder passwordEncoder, 
                      JwtUtil jwtUtil,
                      AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Register a new user
     * @param registerRequest the registration request
     * @return AuthResponse with JWT token and user details
     * @throws RuntimeException if username or email already exists
     */
    public AuthResponse register(RegisterRequest registerRequest) {
        // Check if username already exists
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Username is already taken");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email is already registered");
        }

        // Create new user
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFullName(registerRequest.getFullName());
        user.setIsActive(true);

        // Save user
        User savedUser = userRepository.save(user);

        // Generate JWT tokens
        String token = jwtUtil.generateToken(savedUser.getUsername());
        String refreshToken = jwtUtil.generateToken(savedUser.getUsername()); // For now, same as access token

        return new AuthResponse(token, refreshToken, savedUser.getId(), savedUser.getUsername(), savedUser.getEmail());
    }

    /**
     * Authenticate user and generate JWT token
     * @param loginRequest the login request
     * @return AuthResponse with JWT token and user details
     * @throws RuntimeException if authentication fails
     */
    public AuthResponse login(LoginRequest loginRequest) {
        try {
            // Authenticate user - CustomUserDetailsService now handles both username and email
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),  // Can be either username or email
                    loginRequest.getPassword()
                )
            );

            // Get the authenticated user details
            CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();
            
            // Find the full user details from database
            User user = userRepository.findByUsernameAndIsActiveTrue(userPrincipal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found or inactive"));

            // Generate JWT tokens
            String token = jwtUtil.generateToken(user.getUsername());
            String refreshToken = jwtUtil.generateToken(user.getUsername()); // For now, same as access token

            return new AuthResponse(token, refreshToken, user.getId(), user.getUsername(), user.getEmail());

        } catch (AuthenticationException e) {
            throw new RuntimeException("Invalid username or password");
        }
    }

    /**
     * Refresh JWT token
     * @param token the current JWT token
     * @return AuthResponse with new JWT token
     * @throws RuntimeException if token is invalid or user not found
     */
    public AuthResponse refreshToken(String token) {
        try {
            // Validate current token
            if (!jwtUtil.validateToken(token)) {
                throw new RuntimeException("Invalid token");
            }

            // Extract username from token
            String username = jwtUtil.extractUsername(token);

            // Get user details
            User user = userRepository.findByUsernameAndIsActiveTrue(username)
                .orElseThrow(() -> new RuntimeException("User not found or inactive"));

            // Generate new JWT tokens
            String newToken = jwtUtil.generateToken(user.getUsername());
            String newRefreshToken = jwtUtil.generateToken(user.getUsername()); // For now, same as access token

            return new AuthResponse(newToken, newRefreshToken, user.getId(), user.getUsername(), user.getEmail());

        } catch (Exception e) {
            throw new RuntimeException("Failed to refresh token: " + e.getMessage());
        }
    }

    /**
     * Validate if user exists and is active
     * @param username the username to validate
     * @return true if user exists and is active
     */
    public boolean isUserActive(String username) {
        return userRepository.findByUsernameAndIsActiveTrue(username).isPresent();
    }
}