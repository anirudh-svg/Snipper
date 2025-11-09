package com.snipper.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snipper.dto.snippet.CreateSnippetRequest;
import com.snipper.dto.snippet.UpdateSnippetRequest;
import com.snipper.model.Snippet;
import com.snipper.model.User;
import com.snipper.model.VisibilityType;
import com.snipper.repository.SnippetRepository;
import com.snipper.repository.UserRepository;
import com.snipper.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class SnippetIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SnippetRepository snippetRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private User testUser;
    private User otherUser;
    private String testUserToken;
    private String otherUserToken;
    private Snippet testSnippet;

    @BeforeEach
    void setUp() {
        // Clean up
        snippetRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser = userRepository.save(testUser);

        otherUser = new User();
        otherUser.setUsername("otheruser");
        otherUser.setEmail("other@example.com");
        otherUser.setPassword(passwordEncoder.encode("password123"));
        otherUser = userRepository.save(otherUser);

        // Generate JWT tokens
        testUserToken = jwtUtil.generateToken(testUser.getUsername());
        otherUserToken = jwtUtil.generateToken(otherUser.getUsername());

        // Create test snippet
        testSnippet = new Snippet();
        testSnippet.setTitle("Test Snippet");
        testSnippet.setDescription("Test Description");
        testSnippet.setContent("console.log('Hello World');");
        testSnippet.setLanguage("javascript");
        testSnippet.setTags("test,javascript");
        testSnippet.setVisibility(VisibilityType.PUBLIC);
        testSnippet.setAuthor(testUser);
        testSnippet = snippetRepository.save(testSnippet);
    }

    @Test
    void createSnippet_Success() throws Exception {
        // Given
        CreateSnippetRequest request = new CreateSnippetRequest();
        request.setTitle("New Snippet");
        request.setDescription("New Description");
        request.setContent("print('Hello World')");
        request.setLanguage("python");
        request.setTags("test,python");
        request.setVisibility(VisibilityType.PUBLIC);

        // When & Then
        mockMvc.perform(post("/api/snippets")
                .header("Authorization", "Bearer " + testUserToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Snippet"))
                .andExpect(jsonPath("$.language").value("python"))
                .andExpect(jsonPath("$.authorUsername").value("testuser"));
    }

    @Test
    void createSnippet_Unauthorized() throws Exception {
        // Given
        CreateSnippetRequest request = new CreateSnippetRequest();
        request.setTitle("New Snippet");
        request.setContent("print('Hello World')");
        request.setLanguage("python");
        request.setVisibility(VisibilityType.PUBLIC);

        // When & Then
        mockMvc.perform(post("/api/snippets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getSnippet_Success_Owner() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/snippets/" + testSnippet.getId())
                .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testSnippet.getId()))
                .andExpect(jsonPath("$.title").value("Test Snippet"))
                .andExpect(jsonPath("$.authorUsername").value("testuser"));
    }

    @Test
    void getSnippet_Success_PublicSnippet() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/snippets/" + testSnippet.getId())
                .header("Authorization", "Bearer " + otherUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testSnippet.getId()))
                .andExpect(jsonPath("$.title").value("Test Snippet"));
    }

    @Test
    void getSnippet_Unauthorized_PrivateSnippet() throws Exception {
        // Given - make snippet private
        testSnippet.setVisibility(VisibilityType.PRIVATE);
        snippetRepository.save(testSnippet);

        // When & Then
        mockMvc.perform(get("/api/snippets/" + testSnippet.getId())
                .header("Authorization", "Bearer " + otherUserToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getPublicSnippet_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/snippets/public/" + testSnippet.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testSnippet.getId()))
                .andExpect(jsonPath("$.title").value("Test Snippet"));
    }

    @Test
    void getPublicSnippet_NotFound_PrivateSnippet() throws Exception {
        // Given - make snippet private
        testSnippet.setVisibility(VisibilityType.PRIVATE);
        snippetRepository.save(testSnippet);

        // When & Then
        mockMvc.perform(get("/api/snippets/public/" + testSnippet.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateSnippet_Success() throws Exception {
        // Given
        UpdateSnippetRequest request = new UpdateSnippetRequest();
        request.setTitle("Updated Snippet");
        request.setDescription("Updated Description");
        request.setContent("console.log('Updated Hello World');");
        request.setLanguage("javascript");
        request.setTags("test,javascript,updated");
        request.setVisibility(VisibilityType.PRIVATE);

        // When & Then
        mockMvc.perform(put("/api/snippets/" + testSnippet.getId())
                .header("Authorization", "Bearer " + testUserToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Snippet"))
                .andExpect(jsonPath("$.visibility").value("PRIVATE"));
    }

    @Test
    void updateSnippet_Unauthorized_NotOwner() throws Exception {
        // Given
        UpdateSnippetRequest request = new UpdateSnippetRequest();
        request.setTitle("Updated Snippet");
        request.setContent("console.log('Updated Hello World');");
        request.setLanguage("javascript");
        request.setVisibility(VisibilityType.PRIVATE);

        // When & Then
        mockMvc.perform(put("/api/snippets/" + testSnippet.getId())
                .header("Authorization", "Bearer " + otherUserToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteSnippet_Success() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/snippets/" + testSnippet.getId())
                .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isNoContent());

        // Verify snippet is deleted
        mockMvc.perform(get("/api/snippets/" + testSnippet.getId())
                .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteSnippet_Unauthorized_NotOwner() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/snippets/" + testSnippet.getId())
                .header("Authorization", "Bearer " + otherUserToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void getMySnippets_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/snippets/my")
                .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(testSnippet.getId()))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getPublicSnippets_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/snippets/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(testSnippet.getId()))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void searchPublicSnippets_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/snippets/search")
                .param("q", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(testSnippet.getId()));
    }

    @Test
    void searchMySnippets_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/snippets/my/search")
                .param("q", "Test")
                .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(testSnippet.getId()));
    }

    @Test
    void getSnippetsByLanguage_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/snippets/language/javascript"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(testSnippet.getId()));
    }

    @Test
    void getPopularSnippets_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/snippets/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(testSnippet.getId()));
    }

    @Test
    void getRecentSnippets_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/snippets/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(testSnippet.getId()));
    }

    @Test
    void getAvailableLanguages_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/snippets/languages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").value("javascript"));
    }

    @Test
    void getUserPublicSnippets_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/snippets/user/testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(testSnippet.getId()));
    }

    @Test
    void createSnippet_ValidationError_EmptyTitle() throws Exception {
        // Given
        CreateSnippetRequest request = new CreateSnippetRequest();
        request.setTitle(""); // Invalid empty title
        request.setContent("print('Hello World')");
        request.setLanguage("python");
        request.setVisibility(VisibilityType.PUBLIC);

        // When & Then
        mockMvc.perform(post("/api/snippets")
                .header("Authorization", "Bearer " + testUserToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists());
    }

    @Test
    void createSnippet_ValidationError_EmptyContent() throws Exception {
        // Given
        CreateSnippetRequest request = new CreateSnippetRequest();
        request.setTitle("Valid Title");
        request.setContent(""); // Invalid empty content
        request.setLanguage("python");
        request.setVisibility(VisibilityType.PUBLIC);

        // When & Then
        mockMvc.perform(post("/api/snippets")
                .header("Authorization", "Bearer " + testUserToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.content").exists());
    }

    @Test
    void viewCountIncrement_Success() throws Exception {
        // Given - initial view count should be 0
        Long initialViewCount = testSnippet.getViewCount();

        // When - other user views the snippet
        mockMvc.perform(get("/api/snippets/" + testSnippet.getId())
                .header("Authorization", "Bearer " + otherUserToken))
                .andExpect(status().isOk());

        // Then - view count should be incremented
        Snippet updatedSnippet = snippetRepository.findById(testSnippet.getId()).orElseThrow();
        assert(updatedSnippet.getViewCount() > initialViewCount);
    }

    @Test
    void viewCountNotIncrement_Owner() throws Exception {
        // Given - initial view count should be 0
        Long initialViewCount = testSnippet.getViewCount();

        // When - owner views their own snippet
        mockMvc.perform(get("/api/snippets/" + testSnippet.getId())
                .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk());

        // Then - view count should not be incremented
        Snippet updatedSnippet = snippetRepository.findById(testSnippet.getId()).orElseThrow();
        assert(updatedSnippet.getViewCount().equals(initialViewCount));
    }
}