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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SnippetRepository snippetRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private CustomUserPrincipal userPrincipal;
    private Snippet testSnippet;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFullName("Test User");
        testUser.setBio("Test bio");
        testUser.setCreatedAt(LocalDateTime.now().minusDays(30));
        testUser.setUpdatedAt(LocalDateTime.now());

        userPrincipal = CustomUserPrincipal.create(testUser);

        testSnippet = new Snippet();
        testSnippet.setId(1L);
        testSnippet.setTitle("Test Snippet");
        testSnippet.setDescription("Test description");
        testSnippet.setContent("console.log('Hello World');");
        testSnippet.setLanguage("javascript");
        testSnippet.setTags("test,javascript");
        testSnippet.setVisibility(VisibilityType.PUBLIC);
        testSnippet.setAuthor(testUser);
        testSnippet.setViewCount(10L);
        testSnippet.setCreatedAt(LocalDateTime.now().minusDays(1));
        testSnippet.setUpdatedAt(LocalDateTime.now());

        // Mock security context - will be configured per test as needed
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getCurrentUserProfile_ShouldReturnUserProfile_WhenUserExists() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        UserProfileResponse result = userService.getCurrentUserProfile();

        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getFullName(), result.getFullName());
        assertEquals(testUser.getBio(), result.getBio());
        verify(userRepository).findById(1L);
    }

    @Test
    void getCurrentUserProfile_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> userService.getCurrentUserProfile());
        verify(userRepository).findById(1L);
    }

    @Test
    void getCurrentUserProfile_ShouldThrowException_WhenNotAuthenticated() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // When & Then
        assertThrows(UnauthorizedException.class, () -> userService.getCurrentUserProfile());
    }

    @Test
    void getUserProfile_ShouldReturnUserProfile_WhenUserExists() {
        // Given
        String username = "testuser";
        when(userRepository.findByUsernameAndIsActiveTrue(username)).thenReturn(Optional.of(testUser));

        // When
        UserProfileResponse result = userService.getUserProfile(username);

        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getUsername(), result.getUsername());
        verify(userRepository).findByUsernameAndIsActiveTrue(username);
    }

    @Test
    void getUserProfile_ShouldThrowException_WhenUserNotFound() {
        // Given
        String username = "nonexistent";
        when(userRepository.findByUsernameAndIsActiveTrue(username)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserProfile(username));
        verify(userRepository).findByUsernameAndIsActiveTrue(username);
    }

    @Test
    void updateProfile_ShouldUpdateUsername_WhenUsernameIsUnique() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsernameAndIdNot("newusername", 1L)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setUsername("newusername");

        // When
        UserProfileResponse result = userService.updateProfile(request);

        // Then
        assertNotNull(result);
        verify(userRepository).existsByUsernameAndIdNot("newusername", 1L);
        verify(userRepository).save(testUser);
        assertEquals("newusername", testUser.getUsername());
    }

    @Test
    void updateProfile_ShouldThrowException_WhenUsernameExists() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsernameAndIdNot("existinguser", 1L)).thenReturn(true);

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setUsername("existinguser");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> userService.updateProfile(request));
        verify(userRepository).existsByUsernameAndIdNot("existinguser", 1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateProfile_ShouldUpdateEmail_WhenEmailIsUnique() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmailAndIdNot("new@example.com", 1L)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setEmail("new@example.com");

        // When
        UserProfileResponse result = userService.updateProfile(request);

        // Then
        assertNotNull(result);
        verify(userRepository).existsByEmailAndIdNot("new@example.com", 1L);
        verify(userRepository).save(testUser);
        assertEquals("new@example.com", testUser.getEmail());
    }

    @Test
    void updateProfile_ShouldThrowException_WhenEmailExists() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmailAndIdNot("existing@example.com", 1L)).thenReturn(true);

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setEmail("existing@example.com");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> userService.updateProfile(request));
        verify(userRepository).existsByEmailAndIdNot("existing@example.com", 1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateProfile_ShouldUpdateFullNameAndBio() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("New Full Name");
        request.setBio("New bio");

        // When
        UserProfileResponse result = userService.updateProfile(request);

        // Then
        assertNotNull(result);
        verify(userRepository).save(testUser);
        assertEquals("New Full Name", testUser.getFullName());
        assertEquals("New bio", testUser.getBio());
    }

    @Test
    void getUserDashboard_ShouldReturnDashboardWithStatistics() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        Object[] stats = {5L, 3L, 2L, 0L, 100L}; // total, public, private, unlisted, views
        when(snippetRepository.getSnippetStatisticsByAuthor(testUser)).thenReturn(stats);
        when(snippetRepository.findDistinctLanguagesByAuthor(testUser))
                .thenReturn(Arrays.asList("javascript", "python", "java"));

        // When
        UserDashboardResponse result = userService.getUserDashboard();

        // Then
        assertNotNull(result);
        assertNotNull(result.getProfile());
        assertNotNull(result.getStatistics());
        assertNotNull(result.getRecentLanguages());

        assertEquals(testUser.getUsername(), result.getProfile().getUsername());
        assertEquals(5L, result.getStatistics().getTotalSnippets());
        assertEquals(3L, result.getStatistics().getPublicSnippets());
        assertEquals(2L, result.getStatistics().getPrivateSnippets());
        assertEquals(0L, result.getStatistics().getUnlistedSnippets());
        assertEquals(100L, result.getStatistics().getTotalViews());
        assertEquals(3, result.getRecentLanguages().size());

        verify(userRepository).findById(1L);
        verify(snippetRepository).getSnippetStatisticsByAuthor(testUser);
        verify(snippetRepository).findDistinctLanguagesByAuthor(testUser);
    }

    @Test
    void getUserSnippets_ShouldReturnPagedSnippets() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        List<Snippet> snippets = Arrays.asList(testSnippet);
        Page<Snippet> snippetPage = new PageImpl<>(snippets, PageRequest.of(0, 10), 1);
        when(snippetRepository.findByAuthorOrderByCreatedAtDesc(eq(testUser), any(Pageable.class)))
                .thenReturn(snippetPage);

        // When
        PagedResponse<SnippetSummaryResponse> result = userService.getUserSnippets(
                0, 10, null, "desc", null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(0, result.getPage());
        assertEquals(10, result.getSize());
        assertEquals(1L, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertTrue(result.isLast());

        SnippetSummaryResponse snippetSummary = result.getContent().get(0);
        assertEquals(testSnippet.getId(), snippetSummary.getId());
        assertEquals(testSnippet.getTitle(), snippetSummary.getTitle());
        assertEquals(testSnippet.getLanguage(), snippetSummary.getLanguage());

        verify(userRepository).findById(1L);
        verify(snippetRepository).findByAuthorOrderByCreatedAtDesc(eq(testUser), any(Pageable.class));
    }

    @Test
    void getUserSnippets_ShouldUseSearchWhenProvided() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        List<Snippet> snippets = Arrays.asList(testSnippet);
        Page<Snippet> snippetPage = new PageImpl<>(snippets, PageRequest.of(0, 10), 1);
        when(snippetRepository.searchUserSnippetsWithFilters(
                eq("test"), isNull(), isNull(), isNull(), eq(testUser), any(Pageable.class)))
                .thenReturn(snippetPage);

        // When
        PagedResponse<SnippetSummaryResponse> result = userService.getUserSnippets(
                0, 10, null, "desc", null, null, "test");

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(snippetRepository).searchUserSnippetsWithFilters(
                eq("test"), isNull(), isNull(), isNull(), eq(testUser), any(Pageable.class));
    }

    @Test
    void getPublicSnippetsByUsername_ShouldReturnPublicSnippets() {
        // Given
        String username = "testuser";
        when(userRepository.findByUsernameAndIsActiveTrue(username)).thenReturn(Optional.of(testUser));
        
        List<Snippet> snippets = Arrays.asList(testSnippet);
        Page<Snippet> snippetPage = new PageImpl<>(snippets, PageRequest.of(0, 10), 1);
        when(snippetRepository.findByAuthorAndVisibilityOrderByCreatedAtDesc(
                eq(testUser), eq(VisibilityType.PUBLIC), any(Pageable.class)))
                .thenReturn(snippetPage);

        // When
        PagedResponse<SnippetSummaryResponse> result = userService.getPublicSnippetsByUsername(username, 0, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(userRepository).findByUsernameAndIsActiveTrue(username);
        verify(snippetRepository).findByAuthorAndVisibilityOrderByCreatedAtDesc(
                eq(testUser), eq(VisibilityType.PUBLIC), any(Pageable.class));
    }

    @Test
    void deleteUserSnippet_ShouldDeleteSnippet_WhenUserOwnsSnippet() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(snippetRepository.findByIdAndAuthor(1L, testUser)).thenReturn(Optional.of(testSnippet));

        // When
        userService.deleteUserSnippet(1L);

        // Then
        verify(userRepository).findById(1L);
        verify(snippetRepository).findByIdAndAuthor(1L, testUser);
        verify(snippetRepository).delete(testSnippet);
    }

    @Test
    void deleteUserSnippet_ShouldThrowException_WhenSnippetNotFound() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(snippetRepository.findByIdAndAuthor(1L, testUser)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUserSnippet(1L));
        verify(userRepository).findById(1L);
        verify(snippetRepository).findByIdAndAuthor(1L, testUser);
        verify(snippetRepository, never()).delete(any(Snippet.class));
    }
}