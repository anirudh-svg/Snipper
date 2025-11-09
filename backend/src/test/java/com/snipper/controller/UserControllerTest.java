package com.snipper.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snipper.dto.common.PagedResponse;
import com.snipper.dto.snippet.SnippetSummaryResponse;
import com.snipper.dto.user.UpdateProfileRequest;
import com.snipper.dto.user.UserDashboardResponse;
import com.snipper.dto.user.UserProfileResponse;
import com.snipper.exception.ResourceNotFoundException;
import com.snipper.model.VisibilityType;
import com.snipper.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserProfileResponse userProfile;
    private UserDashboardResponse userDashboard;
    private PagedResponse<SnippetSummaryResponse> pagedSnippets;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        
        userProfile = new UserProfileResponse(
                1L, "testuser", "test@example.com", "Test User", "Test bio", now, now);

        UserDashboardResponse.UserStatistics stats = new UserDashboardResponse.UserStatistics(
                5L, 3L, 2L, 0L, 100L, now);
        userDashboard = new UserDashboardResponse(
                userProfile, stats, Arrays.asList("javascript", "python"));

        SnippetSummaryResponse snippet = new SnippetSummaryResponse(
                1L, "Test Snippet", "Test description", "javascript", "test,js",
                VisibilityType.PUBLIC, 10L, now, now, "testuser", 1L);
        pagedSnippets = new PagedResponse<SnippetSummaryResponse>(
                Collections.singletonList(snippet), 0, 10, 1L, 1, true, true, false, false);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getCurrentUserProfile_ShouldReturnProfile() throws Exception {
        // Given
        when(userService.getCurrentUserProfile()).thenReturn(userProfile);

        // When & Then
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.fullName").value("Test User"))
                .andExpect(jsonPath("$.bio").value("Test bio"));

        verify(userService).getCurrentUserProfile();
    }

    @Test
    void getCurrentUserProfile_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).getCurrentUserProfile();
    }

    @Test
    void getUserProfile_ShouldReturnProfile_WhenUserExists() throws Exception {
        // Given
        String username = "testuser";
        when(userService.getUserProfile(username)).thenReturn(userProfile);

        // When & Then
        mockMvc.perform(get("/api/users/{username}/profile", username))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(userService).getUserProfile(username);
    }

    @Test
    void getUserProfile_ShouldReturn404_WhenUserNotFound() throws Exception {
        // Given
        String username = "nonexistent";
        when(userService.getUserProfile(username))
                .thenThrow(new ResourceNotFoundException("User not found"));

        // When & Then
        mockMvc.perform(get("/api/users/{username}/profile", username))
                .andExpect(status().isNotFound());

        verify(userService).getUserProfile(username);
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateProfile_ShouldReturnUpdatedProfile() throws Exception {
        // Given
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setUsername("newusername");
        request.setFullName("New Full Name");
        request.setBio("New bio");

        UserProfileResponse updatedProfile = new UserProfileResponse(
                1L, "newusername", "test@example.com", "New Full Name", "New bio",
                LocalDateTime.now(), LocalDateTime.now());

        when(userService.updateProfile(any(UpdateProfileRequest.class))).thenReturn(updatedProfile);

        // When & Then
        mockMvc.perform(put("/api/users/profile")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("newusername"))
                .andExpect(jsonPath("$.fullName").value("New Full Name"))
                .andExpect(jsonPath("$.bio").value("New bio"));

        verify(userService).updateProfile(any(UpdateProfileRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateProfile_ShouldReturn400_WhenValidationFails() throws Exception {
        // Given
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setUsername("ab"); // Too short
        request.setEmail("invalid-email"); // Invalid email format

        // When & Then
        mockMvc.perform(put("/api/users/profile")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateProfile(any(UpdateProfileRequest.class));
    }

    @Test
    void updateProfile_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        // Given
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setUsername("newusername");

        // When & Then
        mockMvc.perform(put("/api/users/profile")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).updateProfile(any(UpdateProfileRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getUserDashboard_ShouldReturnDashboard() throws Exception {
        // Given
        when(userService.getUserDashboard()).thenReturn(userDashboard);

        // When & Then
        mockMvc.perform(get("/api/users/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.profile.username").value("testuser"))
                .andExpect(jsonPath("$.statistics.totalSnippets").value(5L))
                .andExpect(jsonPath("$.statistics.publicSnippets").value(3L))
                .andExpect(jsonPath("$.statistics.privateSnippets").value(2L))
                .andExpect(jsonPath("$.statistics.totalViews").value(100L))
                .andExpect(jsonPath("$.recentLanguages").isArray())
                .andExpect(jsonPath("$.recentLanguages[0]").value("javascript"))
                .andExpect(jsonPath("$.recentLanguages[1]").value("python"));

        verify(userService).getUserDashboard();
    }

    @Test
    void getUserDashboard_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users/dashboard"))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).getUserDashboard();
    }

    @Test
    @WithMockUser(roles = "USER")
    void getCurrentUserSnippets_ShouldReturnPagedSnippets() throws Exception {
        // Given
        when(userService.getUserSnippets(0, 10, null, "desc", null, null, null))
                .thenReturn(pagedSnippets);

        // When & Then
        mockMvc.perform(get("/api/users/snippets")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].title").value("Test Snippet"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1L))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.last").value(true));

        verify(userService).getUserSnippets(0, 10, null, "desc", null, null, null);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getCurrentUserSnippets_ShouldApplyFilters() throws Exception {
        // Given
        when(userService.getUserSnippets(0, 10, "title", "asc", "PUBLIC", "javascript", "test"))
                .thenReturn(pagedSnippets);

        // When & Then
        mockMvc.perform(get("/api/users/snippets")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "title")
                        .param("sortDir", "asc")
                        .param("visibility", "PUBLIC")
                        .param("language", "javascript")
                        .param("search", "test"))
                .andExpect(status().isOk());

        verify(userService).getUserSnippets(0, 10, "title", "asc", "PUBLIC", "javascript", "test");
    }

    @Test
    void getCurrentUserSnippets_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users/snippets"))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).getUserSnippets(anyInt(), anyInt(), any(), any(), any(), any(), any());
    }

    @Test
    void getPublicSnippetsByUsername_ShouldReturnPagedSnippets() throws Exception {
        // Given
        String username = "testuser";
        when(userService.getPublicSnippetsByUsername(username, 0, 10)).thenReturn(pagedSnippets);

        // When & Then
        mockMvc.perform(get("/api/users/{username}/snippets", username)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("Test Snippet"));

        verify(userService).getPublicSnippetsByUsername(username, 0, 10);
    }

    @Test
    void getPublicSnippetsByUsername_ShouldReturn404_WhenUserNotFound() throws Exception {
        // Given
        String username = "nonexistent";
        when(userService.getPublicSnippetsByUsername(username, 0, 10))
                .thenThrow(new ResourceNotFoundException("User not found"));

        // When & Then
        mockMvc.perform(get("/api/users/{username}/snippets", username))
                .andExpect(status().isNotFound());

        verify(userService).getPublicSnippetsByUsername(username, 0, 10);
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteUserSnippet_ShouldReturnNoContent() throws Exception {
        // Given
        Long snippetId = 1L;
        doNothing().when(userService).deleteUserSnippet(snippetId);

        // When & Then
        mockMvc.perform(delete("/api/users/snippets/{snippetId}", snippetId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(userService).deleteUserSnippet(snippetId);
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteUserSnippet_ShouldReturn404_WhenSnippetNotFound() throws Exception {
        // Given
        Long snippetId = 999L;
        doThrow(new ResourceNotFoundException("Snippet not found"))
                .when(userService).deleteUserSnippet(snippetId);

        // When & Then
        mockMvc.perform(delete("/api/users/snippets/{snippetId}", snippetId)
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(userService).deleteUserSnippet(snippetId);
    }

    @Test
    void deleteUserSnippet_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        // Given
        Long snippetId = 1L;

        // When & Then
        mockMvc.perform(delete("/api/users/snippets/{snippetId}", snippetId)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).deleteUserSnippet(anyLong());
    }
}