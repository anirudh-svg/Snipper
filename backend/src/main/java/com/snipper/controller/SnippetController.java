package com.snipper.controller;

import com.snipper.dto.common.PagedResponse;
import com.snipper.dto.snippet.CreateSnippetRequest;
import com.snipper.dto.snippet.SnippetResponse;
import com.snipper.dto.snippet.SnippetSummaryResponse;
import com.snipper.dto.snippet.UpdateSnippetRequest;
import com.snipper.service.SnippetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/snippets")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SnippetController {

    private final SnippetService snippetService;

    @Autowired
    public SnippetController(SnippetService snippetService) {
        this.snippetService = snippetService;
    }

    /**
     * Create a new snippet
     */
    @PostMapping
    public ResponseEntity<SnippetResponse> createSnippet(
            @Valid @RequestBody CreateSnippetRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        SnippetResponse response = snippetService.createSnippet(request, username);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get snippet by ID (authenticated)
     */
    @GetMapping("/{id}")
    public ResponseEntity<SnippetResponse> getSnippet(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        SnippetResponse response = snippetService.getSnippetById(id, username);
        return ResponseEntity.ok(response);
    }

    /**
     * Get public snippet by ID (no authentication required)
     */
    @GetMapping("/public/{id}")
    public ResponseEntity<SnippetResponse> getPublicSnippet(@PathVariable Long id) {
        SnippetResponse response = snippetService.getPublicSnippetById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Update snippet
     */
    @PutMapping("/{id}")
    public ResponseEntity<SnippetResponse> updateSnippet(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSnippetRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        SnippetResponse response = snippetService.updateSnippet(id, request, username);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete snippet
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSnippet(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication.getName();
        snippetService.deleteSnippet(id, username);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get user's snippets with pagination and sorting
     */
    @GetMapping("/my")
    public ResponseEntity<PagedResponse<SnippetSummaryResponse>> getMySnippets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Authentication authentication) {
        String username = authentication.getName();
        PagedResponse<SnippetSummaryResponse> response = snippetService.getUserSnippets(username, page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }

    /**
     * Get public snippets with pagination and sorting
     */
    @GetMapping("/public")
    public ResponseEntity<PagedResponse<SnippetSummaryResponse>> getPublicSnippets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        PagedResponse<SnippetSummaryResponse> response = snippetService.getPublicSnippets(page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }

    /**
     * Search public snippets with advanced filtering
     */
    @GetMapping("/search")
    public ResponseEntity<PagedResponse<SnippetSummaryResponse>> searchPublicSnippets(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) String visibility,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        PagedResponse<SnippetSummaryResponse> response = snippetService.searchSnippetsWithFilters(
                q, language, tags, visibility, page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }

    /**
     * Search user's snippets with advanced filtering
     */
    @GetMapping("/my/search")
    public ResponseEntity<PagedResponse<SnippetSummaryResponse>> searchMySnippets(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) String visibility,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Authentication authentication) {
        String username = authentication.getName();
        PagedResponse<SnippetSummaryResponse> response = snippetService.searchUserSnippetsWithFilters(
                q, language, tags, visibility, username, page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }

    /**
     * Get snippets by programming language
     */
    @GetMapping("/language/{language}")
    public ResponseEntity<PagedResponse<SnippetSummaryResponse>> getSnippetsByLanguage(
            @PathVariable String language,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<SnippetSummaryResponse> response = snippetService.getSnippetsByLanguage(language, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * Get most popular public snippets
     */
    @GetMapping("/popular")
    public ResponseEntity<PagedResponse<SnippetSummaryResponse>> getPopularSnippets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<SnippetSummaryResponse> response = snippetService.getMostPopularSnippets(page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * Get recent public snippets
     */
    @GetMapping("/recent")
    public ResponseEntity<PagedResponse<SnippetSummaryResponse>> getRecentSnippets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<SnippetSummaryResponse> response = snippetService.getRecentSnippets(page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * Get available programming languages
     */
    @GetMapping("/languages")
    public ResponseEntity<List<String>> getAvailableLanguages() {
        List<String> languages = snippetService.getAvailableLanguages();
        return ResponseEntity.ok(languages);
    }

    /**
     * Get available tags from public snippets
     */
    @GetMapping("/tags")
    public ResponseEntity<List<String>> getAvailableTags() {
        List<String> tags = snippetService.getAvailableTags();
        return ResponseEntity.ok(tags);
    }

    /**
     * Get snippets by specific user (public only)
     */
    @GetMapping("/user/{username}")
    public ResponseEntity<PagedResponse<SnippetSummaryResponse>> getUserPublicSnippets(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        PagedResponse<SnippetSummaryResponse> response = snippetService.getUserSnippets(username, page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }
}