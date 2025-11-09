package com.snipper.service;

import com.snipper.dto.common.PagedResponse;
import com.snipper.dto.snippet.SnippetSummaryResponse;
import com.snipper.dto.user.UpdateProfileRequest;
import com.snipper.dto.user.UserDashboardResponse;
import com.snipper.dto.user.UserProfileResponse;
import com.snipper.exception.ResourceNotFoundException;
import com.snipper.exception.UnauthorizedException;
import com.snipper.model.Snippet;
import com.snipper.model.User;
import com.snipper.model.VisibilityType;
import com.snipper.repository.SnippetRepository;
import com.snipper.repository.UserRepository;
import com.snipper.security.CustomUserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for user profile and dashboard operations
 */
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final SnippetRepository snippetRepository;

    @Autowired
    public UserService(UserRepository userRepository, SnippetRepository snippetRepository) {
        this.userRepository = userRepository;
        this.snippetRepository = snippetRepository;
    }

    /**
     * Get current authenticated user's profile
     * @return UserProfileResponse containing user profile information
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile() {
        User currentUser = getCurrentUser();
        return mapToUserProfileResponse(currentUser);
    }

    /**
     * Get user profile by username
     * @param username the username to look up
     * @return UserProfileResponse containing user profile information
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(String username) {
        User user = userRepository.findByUsernameAndIsActiveTrue(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return mapToUserProfileResponse(user);
    }

    /**
     * Update current user's profile
     * @param request the update profile request
     * @return UserProfileResponse containing updated profile information
     */
    public UserProfileResponse updateProfile(UpdateProfileRequest request) {
        User currentUser = getCurrentUser();

        // Validate username uniqueness if changed
        if (request.getUsername() != null && !request.getUsername().equals(currentUser.getUsername())) {
            if (userRepository.existsByUsernameAndIdNot(request.getUsername(), currentUser.getId())) {
                throw new IllegalArgumentException("Username already exists: " + request.getUsername());
            }
            currentUser.setUsername(request.getUsername());
        }

        // Validate email uniqueness if changed
        if (request.getEmail() != null && !request.getEmail().equals(currentUser.getEmail())) {
            if (userRepository.existsByEmailAndIdNot(request.getEmail(), currentUser.getId())) {
                throw new IllegalArgumentException("Email already exists: " + request.getEmail());
            }
            currentUser.setEmail(request.getEmail());
        }

        // Update other fields
        if (request.getFullName() != null) {
            currentUser.setFullName(request.getFullName());
        }
        if (request.getBio() != null) {
            currentUser.setBio(request.getBio());
        }

        User updatedUser = userRepository.save(currentUser);
        return mapToUserProfileResponse(updatedUser);
    }

    /**
     * Get current user's dashboard with statistics
     * @return UserDashboardResponse containing profile and statistics
     */
    @Transactional(readOnly = true)
    public UserDashboardResponse getUserDashboard() {
        User currentUser = getCurrentUser();
        UserProfileResponse profile = mapToUserProfileResponse(currentUser);
        
        // Get snippet statistics
        Object[] stats = snippetRepository.getSnippetStatisticsByAuthor(currentUser);
        UserDashboardResponse.UserStatistics statistics = mapToUserStatistics(stats, currentUser);
        
        // Get recent languages used by the user
        List<String> recentLanguages = snippetRepository.findDistinctLanguagesByAuthor(currentUser);
        
        return new UserDashboardResponse(profile, statistics, recentLanguages);
    }

    /**
     * Get user's snippets with pagination and filtering
     * @param page the page number (0-based)
     * @param size the page size
     * @param sortBy the field to sort by (default: createdAt)
     * @param sortDir the sort direction (asc/desc, default: desc)
     * @param visibility optional visibility filter
     * @param language optional language filter
     * @param search optional search term
     * @return PagedResponse containing user's snippets
     */
    @Transactional(readOnly = true)
    public PagedResponse<SnippetSummaryResponse> getUserSnippets(
            int page, int size, String sortBy, String sortDir,
            String visibility, String language, String search) {
        
        User currentUser = getCurrentUser();
        
        // Create sort object
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy != null ? sortBy : "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Snippet> snippetPage;
        
        // Apply filters
        if (search != null && !search.trim().isEmpty()) {
            snippetPage = snippetRepository.searchUserSnippetsWithFilters(
                search.trim(), language, null, visibility, currentUser, pageable);
        } else if (language != null || visibility != null) {
            snippetPage = snippetRepository.searchUserSnippetsWithFilters(
                null, language, null, visibility, currentUser, pageable);
        } else {
            snippetPage = snippetRepository.findByAuthorOrderByCreatedAtDesc(currentUser, pageable);
        }
        
        List<SnippetSummaryResponse> snippetSummaries = snippetPage.getContent().stream()
                .map(this::mapToSnippetSummaryResponse)
                .toList();
        
        return new PagedResponse<SnippetSummaryResponse>(
                snippetSummaries,
                snippetPage.getNumber(),
                snippetPage.getSize(),
                snippetPage.getTotalElements(),
                snippetPage.getTotalPages(),
                snippetPage.isFirst(),
                snippetPage.isLast(),
                snippetPage.hasNext(),
                snippetPage.hasPrevious()
        );
    }

    /**
     * Get public snippets by username with pagination
     * @param username the username
     * @param page the page number (0-based)
     * @param size the page size
     * @return PagedResponse containing user's public snippets
     */
    @Transactional(readOnly = true)
    public PagedResponse<SnippetSummaryResponse> getPublicSnippetsByUsername(String username, int page, int size) {
        User user = userRepository.findByUsernameAndIsActiveTrue(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Snippet> snippetPage = snippetRepository.findByAuthorAndVisibilityOrderByCreatedAtDesc(
                user, VisibilityType.PUBLIC, pageable);
        
        List<SnippetSummaryResponse> snippetSummaries = snippetPage.getContent().stream()
                .map(this::mapToSnippetSummaryResponse)
                .toList();
        
        return new PagedResponse<SnippetSummaryResponse>(
                snippetSummaries,
                snippetPage.getNumber(),
                snippetPage.getSize(),
                snippetPage.getTotalElements(),
                snippetPage.getTotalPages(),
                snippetPage.isFirst(),
                snippetPage.isLast(),
                snippetPage.hasNext(),
                snippetPage.hasPrevious()
        );
    }

    /**
     * Delete user's snippet by ID
     * @param snippetId the snippet ID to delete
     */
    public void deleteUserSnippet(Long snippetId) {
        User currentUser = getCurrentUser();
        
        Snippet snippet = snippetRepository.findByIdAndAuthor(snippetId, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Snippet not found or not owned by user: " + snippetId));
        
        snippetRepository.delete(snippet);
    }

    /**
     * Get current authenticated user
     * @return User entity
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }

        CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();
        return userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userPrincipal.getId()));
    }

    /**
     * Map User entity to UserProfileResponse DTO
     * @param user the User entity
     * @return UserProfileResponse DTO
     */
    private UserProfileResponse mapToUserProfileResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getBio(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    /**
     * Map statistics array to UserStatistics DTO
     * @param stats the statistics array from repository
     * @param user the User entity
     * @return UserStatistics DTO
     */
    private UserDashboardResponse.UserStatistics mapToUserStatistics(Object[] stats, User user) {
        long totalSnippets = stats[0] != null ? ((Number) stats[0]).longValue() : 0;
        long publicSnippets = stats[1] != null ? ((Number) stats[1]).longValue() : 0;
        long privateSnippets = stats[2] != null ? ((Number) stats[2]).longValue() : 0;
        long unlistedSnippets = stats[3] != null ? ((Number) stats[3]).longValue() : 0;
        long totalViews = stats[4] != null ? ((Number) stats[4]).longValue() : 0;
        
        // Use the user's last update time as last activity
        LocalDateTime lastActivity = user.getUpdatedAt();
        
        return new UserDashboardResponse.UserStatistics(
                totalSnippets, publicSnippets, privateSnippets, 
                unlistedSnippets, totalViews, lastActivity
        );
    }

    /**
     * Map Snippet entity to SnippetSummaryResponse DTO
     * @param snippet the Snippet entity
     * @return SnippetSummaryResponse DTO
     */
    private SnippetSummaryResponse mapToSnippetSummaryResponse(Snippet snippet) {
        return new SnippetSummaryResponse(
                snippet.getId(),
                snippet.getTitle(),
                snippet.getDescription(),
                snippet.getLanguage(),
                snippet.getTags(),
                snippet.getVisibility(),
                snippet.getViewCount(),
                snippet.getCreatedAt(),
                snippet.getUpdatedAt(),
                snippet.getAuthor().getUsername(),
                snippet.getAuthor().getId()
        );
    }
}