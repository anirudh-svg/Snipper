package com.snipper.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class SnippetTest {

    private User author;
    private Snippet snippet;

    @BeforeEach
    void setUp() {
        author = new User("testuser", "test@example.com", "hashedpassword");
        author.setId(1L);
        
        snippet = new Snippet("Test Snippet", "console.log('Hello World');", "javascript", VisibilityType.PUBLIC, author);
    }

    @Test
    void testSnippetCreation() {
        assertNotNull(snippet);
        assertEquals("Test Snippet", snippet.getTitle());
        assertEquals("console.log('Hello World');", snippet.getContent());
        assertEquals("javascript", snippet.getLanguage());
        assertEquals(VisibilityType.PUBLIC, snippet.getVisibility());
        assertEquals(author, snippet.getAuthor());
        assertEquals(0L, snippet.getViewCount());
    }

    @Test
    void testSnippetDefaultValues() {
        Snippet newSnippet = new Snippet();
        assertNull(newSnippet.getTitle());
        assertNull(newSnippet.getContent());
        assertNull(newSnippet.getLanguage());
        assertEquals(VisibilityType.PUBLIC, newSnippet.getVisibility());
        assertEquals(0L, newSnippet.getViewCount());
    }

    @Test
    void testSnippetSettersAndGetters() {
        snippet.setDescription("A test snippet");
        snippet.setTags("javascript,test,example");
        snippet.setVisibility(VisibilityType.PRIVATE);

        assertEquals("A test snippet", snippet.getDescription());
        assertEquals("javascript,test,example", snippet.getTags());
        assertEquals(VisibilityType.PRIVATE, snippet.getVisibility());
    }

    @Test
    void testIncrementViewCount() {
        assertEquals(0L, snippet.getViewCount());
        snippet.incrementViewCount();
        assertEquals(1L, snippet.getViewCount());
        snippet.incrementViewCount();
        assertEquals(2L, snippet.getViewCount());
    }

    @Test
    void testIsOwnedBy() {
        assertTrue(snippet.isOwnedBy(author));
        
        User otherUser = new User("otheruser", "other@example.com", "password");
        otherUser.setId(2L);
        assertFalse(snippet.isOwnedBy(otherUser));
        
        assertFalse(snippet.isOwnedBy(null));
    }

    @Test
    void testVisibilityChecks() {
        snippet.setVisibility(VisibilityType.PUBLIC);
        assertTrue(snippet.isPublic());
        assertFalse(snippet.isPrivate());
        assertFalse(snippet.isUnlisted());

        snippet.setVisibility(VisibilityType.PRIVATE);
        assertFalse(snippet.isPublic());
        assertTrue(snippet.isPrivate());
        assertFalse(snippet.isUnlisted());

        snippet.setVisibility(VisibilityType.UNLISTED);
        assertFalse(snippet.isPublic());
        assertFalse(snippet.isPrivate());
        assertTrue(snippet.isUnlisted());
    }

    @Test
    void testSnippetToString() {
        String snippetString = snippet.toString();
        assertTrue(snippetString.contains("Test Snippet"));
        assertTrue(snippetString.contains("javascript"));
        assertTrue(snippetString.contains("public"));
        assertTrue(snippetString.contains("Snippet{"));
    }
}