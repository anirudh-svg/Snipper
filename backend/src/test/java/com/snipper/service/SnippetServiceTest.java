package com.snipper.service;

import com.snipper.dto.snippet.CreateSnippetRequest;
import com.snipper.dto.snippet.SnippetResponse;
import com.snipper.dto.snippet.UpdateSnippetRequest;
import com.snipper.exception.ResourceNotFoundException;
import com.snipper.exception.UnauthorizedException;
import com.snipper.model.Snippet;
import com.snipper.model.User;
import com.snipper.model.VisibilityType;
import com.snipper.repository.SnippetRepository;
import com.snipper.repository.UserRepository;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SnippetServiceTest {

    @Mock
    private SnippetRepository snippetRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SnippetService snippetService;

    private User testUser;
    private User otherUser;
    private Snippet testSnippet;
    private CreateSnippetRequest createRequest;
    private UpdateSnippetRequest updateRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("otheruser");
        otherUser.setEmail("other@example.com");

        testSnippet = new Snippet();
        testSnippet.setId(1L);
        testSnippet.setTitle("Test Snippet");
        testSnippet.setDescription("Test Description");
        testSnippet.setContent("console.log('Hello World');");
        testSnippet.setLanguage("javascript");
        testSnippet.setTags("test,javascript");
        testSnippet.setVisibility(VisibilityType.PUBLIC);
        testSnippet.setViewCount(0L);
        testSnippet.setAuthor(testUser);
        testSnippet.setCreatedAt(LocalDateTime.now());
        testSnippet.setUpdatedAt(LocalDateTime.now());

        createRequest = new CreateSnippetRequest();
        createRequest.setTitle("New Snippet");
        createRequest.setDescription("New Description");
        createRequest.setContent("print('Hello World')");
        createRequest.setLanguage("python");
        createRequest.setTags("test,python");
        createRequest.setVisibility(VisibilityType.PUBLIC);

        updateRequest = new UpdateSnippetRequest();
        updateRequest.setTitle("Updated Snippet");
        updateRequest.setDescription("Updated Description");
        updateRequest.setContent("print('Hello Updated World')");
        updateRequest.setLanguage("python");
        updateRequest.setTags("test,python,updated");
        updateRequest.setVisibility(VisibilityType.PRIVATE);
    }

    @Test
    void createSnippet_Success() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(snippetRepository.save(any(Snippet.class))).thenReturn(testSnippet);

        // When
        SnippetResponse response = snippetService.createSnippet(createRequest, "testuser");

        // Then
        assertNotNull(response);
        assertEquals(testSnippet.getId(), response.getId());
        assertEquals(testSnippet.getTitle(), response.getTitle());
        assertEquals(testSnippet.getAuthor().getUsername(), response.getAuthorUsername());
        verify(userRepository).findByUsername("testuser");
        verify(snippetRepository).save(any(Snippet.class));
    }

    @Test
    void createSnippet_UserNotFound() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, 
            () -> snippetService.createSnippet(createRequest, "nonexistent"));
        verify(userRepository).findByUsername("nonexistent");
        verify(snippetRepository, never()).save(any(Snippet.class));
    }

    @Test
    void getSnippetById_Success_Owner() {
        // Given
        when(snippetRepository.findById(1L)).thenReturn(Optional.of(testSnippet));

        // When
        SnippetResponse response = snippetService.getSnippetById(1L, "testuser");

        // Then
        assertNotNull(response);
        assertEquals(testSnippet.getId(), response.getId());
        assertEquals(testSnippet.getTitle(), response.getTitle());
        verify(snippetRepository).findById(1L);
        verify(snippetRepository, never()).incrementViewCount(1L); // Owner doesn't increment view count
    }

    @Test
    void getSnippetById_Success_PublicSnippet() {
        // Given
        when(snippetRepository.findById(1L)).thenReturn(Optional.of(testSnippet));

        // When
        SnippetResponse response = snippetService.getSnippetById(1L, "otheruser");

        // Then
        assertNotNull(response);
        assertEquals(testSnippet.getId(), response.getId());
        verify(snippetRepository).findById(1L);
        verify(snippetRepository).incrementViewCount(1L); // Non-owner increments view count
    }

    @Test
    void getSnippetById_PrivateSnippet_Unauthorized() {
        // Given
        testSnippet.setVisibility(VisibilityType.PRIVATE);
        when(snippetRepository.findById(1L)).thenReturn(Optional.of(testSnippet));

        // When & Then
        assertThrows(UnauthorizedException.class, 
            () -> snippetService.getSnippetById(1L, "otheruser"));
        verify(snippetRepository).findById(1L);
        verify(snippetRepository, never()).incrementViewCount(1L);
    }

    @Test
    void getSnippetById_NotFound() {
        // Given
        when(snippetRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, 
            () -> snippetService.getSnippetById(999L, "testuser"));
        verify(snippetRepository).findById(999L);
    }

    @Test
    void getPublicSnippetById_Success() {
        // Given
        when(snippetRepository.findPublicOrUnlistedById(1L)).thenReturn(Optional.of(testSnippet));

        // When
        SnippetResponse response = snippetService.getPublicSnippetById(1L);

        // Then
        assertNotNull(response);
        assertEquals(testSnippet.getId(), response.getId());
        verify(snippetRepository).findPublicOrUnlistedById(1L);
        verify(snippetRepository).incrementViewCount(1L);
    }

    @Test
    void getPublicSnippetById_NotFound() {
        // Given
        when(snippetRepository.findPublicOrUnlistedById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, 
            () -> snippetService.getPublicSnippetById(999L));
        verify(snippetRepository).findPublicOrUnlistedById(999L);
    }

    @Test
    void updateSnippet_Success() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(snippetRepository.findByIdAndAuthor(1L, testUser)).thenReturn(Optional.of(testSnippet));
        when(snippetRepository.save(any(Snippet.class))).thenReturn(testSnippet);

        // When
        SnippetResponse response = snippetService.updateSnippet(1L, updateRequest, "testuser");

        // Then
        assertNotNull(response);
        verify(userRepository).findByUsername("testuser");
        verify(snippetRepository).findByIdAndAuthor(1L, testUser);
        verify(snippetRepository).save(testSnippet);
    }

    @Test
    void updateSnippet_NotOwner() {
        // Given
        when(userRepository.findByUsername("otheruser")).thenReturn(Optional.of(otherUser));
        when(snippetRepository.findByIdAndAuthor(1L, otherUser)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, 
            () -> snippetService.updateSnippet(1L, updateRequest, "otheruser"));
        verify(userRepository).findByUsername("otheruser");
        verify(snippetRepository).findByIdAndAuthor(1L, otherUser);
        verify(snippetRepository, never()).save(any(Snippet.class));
    }

    @Test
    void deleteSnippet_Success() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(snippetRepository.findByIdAndAuthor(1L, testUser)).thenReturn(Optional.of(testSnippet));

        // When
        snippetService.deleteSnippet(1L, "testuser");

        // Then
        verify(userRepository).findByUsername("testuser");
        verify(snippetRepository).findByIdAndAuthor(1L, testUser);
        verify(snippetRepository).delete(testSnippet);
    }

    @Test
    void deleteSnippet_NotOwner() {
        // Given
        when(userRepository.findByUsername("otheruser")).thenReturn(Optional.of(otherUser));
        when(snippetRepository.findByIdAndAuthor(1L, otherUser)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, 
            () -> snippetService.deleteSnippet(1L, "otheruser"));
        verify(userRepository).findByUsername("otheruser");
        verify(snippetRepository).findByIdAndAuthor(1L, otherUser);
        verify(snippetRepository, never()).delete(any(Snippet.class));
    }

    @Test
    void getUserSnippets_Success() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        Page<Snippet> snippetPage = new PageImpl<>(Arrays.asList(testSnippet));
        when(snippetRepository.findByAuthorOrderByCreatedAtDesc(eq(testUser), any(Pageable.class)))
            .thenReturn(snippetPage);

        // When
        var response = snippetService.getUserSnippets("testuser", 0, 10, "createdAt", "desc");

        // Then
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(testSnippet.getId(), response.getContent().get(0).getId());
        verify(userRepository).findByUsername("testuser");
        verify(snippetRepository).findByAuthorOrderByCreatedAtDesc(eq(testUser), any(Pageable.class));
    }

    @Test
    void getPublicSnippets_Success() {
        // Given
        Page<Snippet> snippetPage = new PageImpl<>(Arrays.asList(testSnippet));
        when(snippetRepository.findByVisibilityOrderByCreatedAtDesc(eq(VisibilityType.PUBLIC), any(Pageable.class)))
            .thenReturn(snippetPage);

        // When
        var response = snippetService.getPublicSnippets(0, 10, "createdAt", "desc");

        // Then
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(testSnippet.getId(), response.getContent().get(0).getId());
        verify(snippetRepository).findByVisibilityOrderByCreatedAtDesc(eq(VisibilityType.PUBLIC), any(Pageable.class));
    }

    @Test
    void searchPublicSnippets_Success() {
        // Given
        Page<Snippet> snippetPage = new PageImpl<>(Arrays.asList(testSnippet));
        when(snippetRepository.searchPublicSnippets(eq("test"), any(Pageable.class)))
            .thenReturn(snippetPage);

        // When
        var response = snippetService.searchPublicSnippets("test", 0, 10);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(testSnippet.getId(), response.getContent().get(0).getId());
        verify(snippetRepository).searchPublicSnippets(eq("test"), any(Pageable.class));
    }

    @Test
    void searchUserSnippets_Success() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        Page<Snippet> snippetPage = new PageImpl<>(Arrays.asList(testSnippet));
        when(snippetRepository.searchUserSnippets(eq("test"), eq(testUser), any(Pageable.class)))
            .thenReturn(snippetPage);

        // When
        var response = snippetService.searchUserSnippets("test", "testuser", 0, 10);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(testSnippet.getId(), response.getContent().get(0).getId());
        verify(userRepository).findByUsername("testuser");
        verify(snippetRepository).searchUserSnippets(eq("test"), eq(testUser), any(Pageable.class));
    }

    @Test
    void getSnippetsByLanguage_Success() {
        // Given
        Page<Snippet> snippetPage = new PageImpl<>(Arrays.asList(testSnippet));
        when(snippetRepository.findPublicSnippetsByLanguage(eq("javascript"), any(Pageable.class)))
            .thenReturn(snippetPage);

        // When
        var response = snippetService.getSnippetsByLanguage("javascript", 0, 10);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(testSnippet.getId(), response.getContent().get(0).getId());
        verify(snippetRepository).findPublicSnippetsByLanguage(eq("javascript"), any(Pageable.class));
    }

    @Test
    void getMostPopularSnippets_Success() {
        // Given
        Page<Snippet> snippetPage = new PageImpl<>(Arrays.asList(testSnippet));
        when(snippetRepository.findMostPopularPublicSnippets(any(Pageable.class)))
            .thenReturn(snippetPage);

        // When
        var response = snippetService.getMostPopularSnippets(0, 10);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(testSnippet.getId(), response.getContent().get(0).getId());
        verify(snippetRepository).findMostPopularPublicSnippets(any(Pageable.class));
    }

    @Test
    void getRecentSnippets_Success() {
        // Given
        Page<Snippet> snippetPage = new PageImpl<>(Arrays.asList(testSnippet));
        when(snippetRepository.findRecentPublicSnippets(any(Pageable.class)))
            .thenReturn(snippetPage);

        // When
        var response = snippetService.getRecentSnippets(0, 10);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(testSnippet.getId(), response.getContent().get(0).getId());
        verify(snippetRepository).findRecentPublicSnippets(any(Pageable.class));
    }

    @Test
    void getAvailableLanguages_Success() {
        // Given
        when(snippetRepository.findDistinctLanguagesFromPublicSnippets())
            .thenReturn(Arrays.asList("javascript", "python", "java"));

        // When
        var languages = snippetService.getAvailableLanguages();

        // Then
        assertNotNull(languages);
        assertEquals(3, languages.size());
        assertTrue(languages.contains("javascript"));
        assertTrue(languages.contains("python"));
        assertTrue(languages.contains("java"));
        verify(snippetRepository).findDistinctLanguagesFromPublicSnippets();
    }

    @Test
    void getAvailableTags_Success() {
        // Given
        when(snippetRepository.findDistinctTagsFromPublicSnippets())
            .thenReturn(Arrays.asList("test,javascript", "python,web", "java,spring"));

        // When
        var tags = snippetService.getAvailableTags();

        // Then
        assertNotNull(tags);
        assertEquals(6, tags.size()); // test, javascript, python, web, java, spring = 6 tags
        assertTrue(tags.contains("test"));
        assertTrue(tags.contains("javascript"));
        assertTrue(tags.contains("python"));
        assertTrue(tags.contains("web"));
        assertTrue(tags.contains("java"));
        assertTrue(tags.contains("spring"));
        verify(snippetRepository).findDistinctTagsFromPublicSnippets();
    }

    @Test
    void searchSnippetsWithFilters_Success() {
        // Given
        Page<Snippet> snippetPage = new PageImpl<>(Arrays.asList(testSnippet));
        when(snippetRepository.searchSnippetsWithFilters(eq("test"), eq("javascript"), eq("test"), any(Pageable.class)))
            .thenReturn(snippetPage);

        // When
        var response = snippetService.searchSnippetsWithFilters("test", "javascript", "test", null, 0, 10, "createdAt", "desc");

        // Then
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(testSnippet.getId(), response.getContent().get(0).getId());
        verify(snippetRepository).searchSnippetsWithFilters(eq("test"), eq("javascript"), eq("test"), any(Pageable.class));
    }

    @Test
    void searchSnippetsWithFilters_NoFilters() {
        // Given
        Page<Snippet> snippetPage = new PageImpl<>(Arrays.asList(testSnippet));
        when(snippetRepository.searchSnippetsWithFilters(eq(null), eq(null), eq(null), any(Pageable.class)))
            .thenReturn(snippetPage);

        // When
        var response = snippetService.searchSnippetsWithFilters(null, null, null, null, 0, 10, "createdAt", "desc");

        // Then
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(testSnippet.getId(), response.getContent().get(0).getId());
        verify(snippetRepository).searchSnippetsWithFilters(eq(null), eq(null), eq(null), any(Pageable.class));
    }

    @Test
    void searchUserSnippetsWithFilters_Success() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        Page<Snippet> snippetPage = new PageImpl<>(Arrays.asList(testSnippet));
        when(snippetRepository.searchUserSnippetsWithFilters(eq("test"), eq("javascript"), eq("test"), eq("PUBLIC"), eq(testUser), any(Pageable.class)))
            .thenReturn(snippetPage);

        // When
        var response = snippetService.searchUserSnippetsWithFilters("test", "javascript", "test", "PUBLIC", "testuser", 0, 10, "createdAt", "desc");

        // Then
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(testSnippet.getId(), response.getContent().get(0).getId());
        verify(userRepository).findByUsername("testuser");
        verify(snippetRepository).searchUserSnippetsWithFilters(eq("test"), eq("javascript"), eq("test"), eq("PUBLIC"), eq(testUser), any(Pageable.class));
    }

    @Test
    void searchUserSnippetsWithFilters_UserNotFound() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, 
            () -> snippetService.searchUserSnippetsWithFilters("test", null, null, null, "nonexistent", 0, 10, "createdAt", "desc"));
        verify(userRepository).findByUsername("nonexistent");
        verify(snippetRepository, never()).searchUserSnippetsWithFilters(any(), any(), any(), any(), any(), any());
    }
}