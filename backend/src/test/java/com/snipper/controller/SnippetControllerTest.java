package com.snipper.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snipper.dto.common.PagedResponse;
import com.snipper.dto.snippet.CreateSnippetRequest;
import com.snipper.dto.snippet.SnippetResponse;
import com.snipper.dto.snippet.SnippetSummaryResponse;
import com.snipper.dto.snippet.UpdateSnippetRequest;
import com.snipper.exception.ResourceNotFoundException;
import com.snipper.exception.UnauthorizedException;
import com.snipper.model.VisibilityType;
import com.snipper.service.SnippetService;
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
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SnippetController.class)
class SnippetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SnippetService snippetService;

    @Autowired
    private ObjectMapper objectMapper;

    private SnippetResponse snippetResponse;
    private SnippetSummaryResponse snippetSummaryResponse;
    private CreateSnippetRequest createRequest;
    private UpdateSnippetRequest updateRequest;
    private PagedResponse<SnippetSummaryResponse> pagedResponse;

    @BeforeEach
    void setUp() {
        snippetResponse = new SnippetResponse(
                1L,
                "Test Snippet",
                "Test Description",
                "console.log('Hello World');",
                "javascript",
                "test,javascript",
                VisibilityType.PUBLIC,
                0L,
                LocalDateTime.now(),
                LocalDateTime.now(),
                "testuser",
                1L
        );

        snippetSummaryResponse = new SnippetSummaryResponse(
                1L,
                "Test Snippet",
                "Test Description",
                "javascript",
                "test,javascript",
                VisibilityType.PUBLIC,
                0L,
                LocalDateTime.now(),
                LocalDateTime.now(),
                "testuser",
                1L
        );

        createRequest = new CreateSnippetRequest(
                "New Snippet",
                "New Description",
                "print('Hello World')",
                "python",
                "test,python",
                VisibilityType.PUBLIC
        );

        updateRequest = new UpdateSnippetRequest(
                "Updated Snippet",
                "Updated Description",
                "print('Hello Updated World')",
                "python",
                "test,python,updated",
                VisibilityType.PRIVATE
        );

        pagedResponse = new PagedResponse<>(
                Arrays.asList(snippetSummaryResponse),
                0,
                10,
                1L,
                1,
                true,
                true,
                false,
                false
        );
    }

    @Test
    @WithMockUser(username = "testuser")
    void createSnippet_Success() throws Exception {
        // Given
        when(snippetService.createSnippet(any(CreateSnippetRequest.class), eq("testuser")))
                .thenReturn(snippetResponse);

        // When & Then
        mockMvc.perform(post("/api/snippets")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Snippet"))
                .andExpect(jsonPath("$.authorUsername").value("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void createSnippet_ValidationError() throws Exception {
        // Given
        createRequest.setTitle(""); // Invalid title

        // When & Then
        mockMvc.perform(post("/api/snippets")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser")
    void getSnippet_Success() throws Exception {
        // Given
        when(snippetService.getSnippetById(1L, "testuser")).thenReturn(snippetResponse);

        // When & Then
        mockMvc.perform(get("/api/snippets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Snippet"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getSnippet_NotFound() throws Exception {
        // Given
        when(snippetService.getSnippetById(999L, "testuser"))
                .thenThrow(new ResourceNotFoundException("Snippet not found"));

        // When & Then
        mockMvc.perform(get("/api/snippets/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "otheruser")
    void getSnippet_Unauthorized() throws Exception {
        // Given
        when(snippetService.getSnippetById(1L, "otheruser"))
                .thenThrow(new UnauthorizedException("Access denied"));

        // When & Then
        mockMvc.perform(get("/api/snippets/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getPublicSnippet_Success() throws Exception {
        // Given
        when(snippetService.getPublicSnippetById(1L)).thenReturn(snippetResponse);

        // When & Then
        mockMvc.perform(get("/api/snippets/public/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Snippet"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateSnippet_Success() throws Exception {
        // Given
        when(snippetService.updateSnippet(eq(1L), any(UpdateSnippetRequest.class), eq("testuser")))
                .thenReturn(snippetResponse);

        // When & Then
        mockMvc.perform(put("/api/snippets/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteSnippet_Success() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/snippets/1")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "testuser")
    void getMySnippets_Success() throws Exception {
        // Given
        when(snippetService.getUserSnippets("testuser", 0, 10, "createdAt", "desc"))
                .thenReturn(pagedResponse);

        // When & Then
        mockMvc.perform(get("/api/snippets/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.totalElements").value(1L));
    }

    @Test
    void getPublicSnippets_Success() throws Exception {
        // Given
        when(snippetService.getPublicSnippets(0, 10, "createdAt", "desc"))
                .thenReturn(pagedResponse);

        // When & Then
        mockMvc.perform(get("/api/snippets/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1L));
    }

    @Test
    void searchPublicSnippets_Success() throws Exception {
        // Given
        when(snippetService.searchSnippetsWithFilters("test", null, null, null, 0, 10, "createdAt", "desc"))
                .thenReturn(pagedResponse);

        // When & Then
        mockMvc.perform(get("/api/snippets/search")
                .param("q", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1L));
    }

    @Test
    @WithMockUser(username = "testuser")
    void searchMySnippets_Success() throws Exception {
        // Given
        when(snippetService.searchUserSnippetsWithFilters("test", null, null, null, "testuser", 0, 10, "createdAt", "desc"))
                .thenReturn(pagedResponse);

        // When & Then
        mockMvc.perform(get("/api/snippets/my/search")
                .param("q", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1L));
    }

    @Test
    void getSnippetsByLanguage_Success() throws Exception {
        // Given
        when(snippetService.getSnippetsByLanguage("javascript", 0, 10))
                .thenReturn(pagedResponse);

        // When & Then
        mockMvc.perform(get("/api/snippets/language/javascript"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1L));
    }

    @Test
    void getPopularSnippets_Success() throws Exception {
        // Given
        when(snippetService.getMostPopularSnippets(0, 10))
                .thenReturn(pagedResponse);

        // When & Then
        mockMvc.perform(get("/api/snippets/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1L));
    }

    @Test
    void getRecentSnippets_Success() throws Exception {
        // Given
        when(snippetService.getRecentSnippets(0, 10))
                .thenReturn(pagedResponse);

        // When & Then
        mockMvc.perform(get("/api/snippets/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1L));
    }

    @Test
    void getAvailableLanguages_Success() throws Exception {
        // Given
        when(snippetService.getAvailableLanguages())
                .thenReturn(Arrays.asList("javascript", "python", "java"));

        // When & Then
        mockMvc.perform(get("/api/snippets/languages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").value("javascript"))
                .andExpect(jsonPath("$[1]").value("python"))
                .andExpect(jsonPath("$[2]").value("java"));
    }

    @Test
    void getUserPublicSnippets_Success() throws Exception {
        // Given
        when(snippetService.getUserSnippets("testuser", 0, 10, "createdAt", "desc"))
                .thenReturn(pagedResponse);

        // When & Then
        mockMvc.perform(get("/api/snippets/user/testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1L));
    }

    @Test
    void getAvailableTags_Success() throws Exception {
        // Given
        when(snippetService.getAvailableTags())
                .thenReturn(Arrays.asList("javascript", "python", "web"));

        // When & Then
        mockMvc.perform(get("/api/snippets/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").value("javascript"))
                .andExpect(jsonPath("$[1]").value("python"))
                .andExpect(jsonPath("$[2]").value("web"));
    }

    @Test
    void searchPublicSnippetsWithFilters_Success() throws Exception {
        // Given
        when(snippetService.searchSnippetsWithFilters("test", "javascript", "web", null, 0, 10, "createdAt", "desc"))
                .thenReturn(pagedResponse);

        // When & Then
        mockMvc.perform(get("/api/snippets/search")
                .param("q", "test")
                .param("language", "javascript")
                .param("tags", "web"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1L));
    }

    @Test
    void searchPublicSnippetsWithFilters_NoParams() throws Exception {
        // Given
        when(snippetService.searchSnippetsWithFilters(null, null, null, null, 0, 10, "createdAt", "desc"))
                .thenReturn(pagedResponse);

        // When & Then
        mockMvc.perform(get("/api/snippets/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1L));
    }

    @Test
    @WithMockUser(username = "testuser")
    void searchMySnippetsWithFilters_Success() throws Exception {
        // Given
        when(snippetService.searchUserSnippetsWithFilters("test", "python", "web", "PUBLIC", "testuser", 0, 10, "createdAt", "desc"))
                .thenReturn(pagedResponse);

        // When & Then
        mockMvc.perform(get("/api/snippets/my/search")
                .param("q", "test")
                .param("language", "python")
                .param("tags", "web")
                .param("visibility", "PUBLIC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1L));
    }

    @Test
    @WithMockUser(username = "testuser")
    void searchMySnippetsWithFilters_NoParams() throws Exception {
        // Given
        when(snippetService.searchUserSnippetsWithFilters(null, null, null, null, "testuser", 0, 10, "createdAt", "desc"))
                .thenReturn(pagedResponse);

        // When & Then
        mockMvc.perform(get("/api/snippets/my/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1L));
    }
}