package com.snipper.controller;

import com.snipper.dto.common.PagedResponse;
import com.snipper.dto.snippet.SnippetSummaryResponse;
import com.snipper.dto.user.UpdateProfileRequest;
import com.snipper.dto.user.UserDashboardResponse;
import com.snipper.dto.user.UserProfileResponse;
import com.snipper.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for user profile and dashboard operations
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get current authenticated user's profile
     * @return ResponseEntity containing user profile
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserProfileResponse> getCurrentUserProfile() {
        UserProfileResponse profile = userService.getCurrentUserProfile();
        return ResponseEntity.ok(profile);
    }

    /**
     * Get user profile by username (public endpoint)
     * @param username the username to look up
     * @return ResponseEntity containing user profile
     */
    @GetMapping("/{username}/profile")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable String username) {
        UserProfileResponse profile = userService.getUserProfile(username);
        return ResponseEntity.ok(profile);
    }

    /**
     * Update current user's profile
     * @param request the update profile request
     * @return ResponseEntity containing updated profile
     */
    @PutMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        UserProfileResponse updatedProfile = userService.updateProfile(request);
        return ResponseEntity.ok(updatedProfile);
    }

    /**
     * Get current user's dashboard with statistics
     * @return ResponseEntity containing dashboard data
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserDashboardResponse> getUserDashboard() {
        UserDashboardResponse dashboard = userService.getUserDashboard();
        return ResponseEntity.ok(dashboard);
    }

    /**
     * Get current user's snippets with pagination and filtering
     * @param page the page number (default: 0)
     * @param size the page size (default: 10)
     * @param sortBy the field to sort by (default: createdAt)
     * @param sortDir the sort direction (default: desc)
     * @param visibility optional visibility filter
     * @param language optional language filter
     * @param search optional search term
     * @return ResponseEntity containing paginated snippets
     */
    @GetMapping("/snippets")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PagedResponse<SnippetSummaryResponse>> getCurrentUserSnippets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String visibility,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String search) {
        
        PagedResponse<SnippetSummaryResponse> snippets = userService.getUserSnippets(
                page, size, sortBy, sortDir, visibility, language, search);
        return ResponseEntity.ok(snippets);
    }

    /**
     * Get public snippets by username with pagination
     * @param username the username
     * @param page the page number (default: 0)
     * @param size the page size (default: 10)
     * @return ResponseEntity containing paginated public snippets
     */
    @GetMapping("/{username}/snippets")
    public ResponseEntity<PagedResponse<SnippetSummaryResponse>> getPublicSnippetsByUsername(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        PagedResponse<SnippetSummaryResponse> snippets = userService.getPublicSnippetsByUsername(username, page, size);
        return ResponseEntity.ok(snippets);
    }

    /**
     * Delete user's snippet by ID
     * @param snippetId the snippet ID to delete
     * @return ResponseEntity with no content
     */
    @DeleteMapping("/snippets/{snippetId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteUserSnippet(@PathVariable Long snippetId) {
        userService.deleteUserSnippet(snippetId);
        return ResponseEntity.noContent().build();
    }
}