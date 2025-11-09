package com.snipper.service;

import com.snipper.dto.common.PagedResponse;
import com.snipper.dto.snippet.CreateSnippetRequest;
import com.snipper.dto.snippet.SnippetResponse;
import com.snipper.dto.snippet.SnippetSummaryResponse;
import com.snipper.dto.snippet.UpdateSnippetRequest;
import com.snipper.exception.ResourceNotFoundException;
import com.snipper.exception.UnauthorizedException;
import com.snipper.model.Snippet;
import com.snipper.model.User;
import com.snipper.model.VisibilityType;
import com.snipper.repository.SnippetRepository;
import com.snipper.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SnippetService {

    private final SnippetRepository snippetRepository;
    private final UserRepository userRepository;

    @Autowired
    public SnippetService(SnippetRepository snippetRepository, UserRepository userRepository) {
        this.snippetRepository = snippetRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create a new snippet
     */
    public SnippetResponse createSnippet(CreateSnippetRequest request, String username) {
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Snippet snippet = new Snippet();
        snippet.setTitle(request.getTitle());
        snippet.setDescription(request.getDescription());
        snippet.setContent(request.getContent());
        snippet.setLanguage(request.getLanguage());
        snippet.setTags(request.getTags());
        snippet.setVisibility(request.getVisibility());
        snippet.setAuthor(author);

        Snippet savedSnippet = snippetRepository.save(snippet);
        return convertToSnippetResponse(savedSnippet);
    }

    /**
     * Get snippet by ID with authorization check
     */
    @Transactional(readOnly = true)
    public SnippetResponse getSnippetById(Long id, String username) {
        Snippet snippet = snippetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Snippet not found with id: " + id));

        // Check if user can access this snippet
        if (!canUserAccessSnippet(snippet, username)) {
            throw new UnauthorizedException("You don't have permission to access this snippet");
        }

        // Increment view count if it's not the owner viewing
        if (!snippet.getAuthor().getUsername().equals(username)) {
            snippetRepository.incrementViewCount(id);
            snippet.incrementViewCount(); // Update the entity for response
        }

        return convertToSnippetResponse(snippet);
    }

    /**
     * Get public snippet by ID (no authentication required)
     */
    @Transactional(readOnly = true)
    public SnippetResponse getPublicSnippetById(Long id) {
        Snippet snippet = snippetRepository.findPublicOrUnlistedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Public snippet not found with id: " + id));

        // Increment view count
        snippetRepository.incrementViewCount(id);
        snippet.incrementViewCount(); // Update the entity for response

        return convertToSnippetResponse(snippet);
    }

    /**
     * Update snippet
     */
    public SnippetResponse updateSnippet(Long id, UpdateSnippetRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Snippet snippet = snippetRepository.findByIdAndAuthor(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Snippet not found or you don't have permission to edit it"));

        snippet.setTitle(request.getTitle());
        snippet.setDescription(request.getDescription());
        snippet.setContent(request.getContent());
        snippet.setLanguage(request.getLanguage());
        snippet.setTags(request.getTags());
        snippet.setVisibility(request.getVisibility());

        Snippet updatedSnippet = snippetRepository.save(snippet);
        return convertToSnippetResponse(updatedSnippet);
    }

    /**
     * Delete snippet
     */
    public void deleteSnippet(Long id, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Snippet snippet = snippetRepository.findByIdAndAuthor(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Snippet not found or you don't have permission to delete it"));

        snippetRepository.delete(snippet);
    }

    /**
     * Get user's snippets with pagination
     */
    @Transactional(readOnly = true)
    public PagedResponse<SnippetSummaryResponse> getUserSnippets(String username, int page, int size, String sortBy, String sortDir) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Snippet> snippetPage = snippetRepository.findByAuthorOrderByCreatedAtDesc(user, pageable);
        return convertToPagedSummaryResponse(snippetPage);
    }

    /**
     * Get public snippets with pagination
     */
    @Transactional(readOnly = true)
    public PagedResponse<SnippetSummaryResponse> getPublicSnippets(int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Snippet> snippetPage = snippetRepository.findByVisibilityOrderByCreatedAtDesc(VisibilityType.PUBLIC, pageable);
        return convertToPagedSummaryResponse(snippetPage);
    }



    /**
     * Get snippets by language
     */
    @Transactional(readOnly = true)
    public PagedResponse<SnippetSummaryResponse> getSnippetsByLanguage(String language, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Snippet> snippetPage = snippetRepository.findPublicSnippetsByLanguage(language, pageable);
        return convertToPagedSummaryResponse(snippetPage);
    }

    /**
     * Get most popular public snippets
     */
    @Transactional(readOnly = true)
    public PagedResponse<SnippetSummaryResponse> getMostPopularSnippets(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Snippet> snippetPage = snippetRepository.findMostPopularPublicSnippets(pageable);
        return convertToPagedSummaryResponse(snippetPage);
    }

    /**
     * Get recent public snippets
     */
    @Transactional(readOnly = true)
    public PagedResponse<SnippetSummaryResponse> getRecentSnippets(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Snippet> snippetPage = snippetRepository.findRecentPublicSnippets(pageable);
        return convertToPagedSummaryResponse(snippetPage);
    }

    /**
     * Get distinct languages from public snippets
     */
    @Transactional(readOnly = true)
    public List<String> getAvailableLanguages() {
        return snippetRepository.findDistinctLanguagesFromPublicSnippets();
    }

    /**
     * Get distinct tags from public snippets
     */
    @Transactional(readOnly = true)
    public List<String> getAvailableTags() {
        List<String> tagStrings = snippetRepository.findDistinctTagsFromPublicSnippets();
        return tagStrings.stream()
                .flatMap(tagString -> Arrays.stream(tagString.split(",")))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Search snippets with advanced filtering
     */
    @Transactional(readOnly = true)
    public PagedResponse<SnippetSummaryResponse> searchSnippetsWithFilters(
            String searchTerm, String language, String tags, String visibility,
            int page, int size, String sortBy, String sortDir) {
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Snippet> snippetPage = snippetRepository.searchSnippetsWithFilters(
                searchTerm, language, tags, pageable);
        return convertToPagedSummaryResponse(snippetPage);
    }

    /**
     * Search user's snippets with advanced filtering
     */
    @Transactional(readOnly = true)
    public PagedResponse<SnippetSummaryResponse> searchUserSnippetsWithFilters(
            String searchTerm, String language, String tags, String visibility, String username,
            int page, int size, String sortBy, String sortDir) {
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Snippet> snippetPage = snippetRepository.searchUserSnippetsWithFilters(
                searchTerm, language, tags, visibility, user, pageable);
        return convertToPagedSummaryResponse(snippetPage);
    }

    /**
     * Search public snippets (legacy method for backward compatibility)
     */
    @Transactional(readOnly = true)
    public PagedResponse<SnippetSummaryResponse> searchPublicSnippets(String searchTerm, int page, int size) {
        return searchSnippetsWithFilters(searchTerm, null, null, null, page, size, "createdAt", "desc");
    }

    /**
     * Search user's snippets (legacy method for backward compatibility)
     */
    @Transactional(readOnly = true)
    public PagedResponse<SnippetSummaryResponse> searchUserSnippets(String searchTerm, String username, int page, int size) {
        return searchUserSnippetsWithFilters(searchTerm, null, null, null, username, page, size, "createdAt", "desc");
    }

    /**
     * Check if user can access a snippet
     */
    private boolean canUserAccessSnippet(Snippet snippet, String username) {
        // Public snippets are accessible to everyone
        if (snippet.getVisibility() == VisibilityType.PUBLIC) {
            return true;
        }

        // Private snippets are only accessible to the owner
        if (username == null) {
            return false;
        }

        return snippet.getAuthor().getUsername().equals(username);
    }

    /**
     * Convert Snippet entity to SnippetResponse DTO
     */
    private SnippetResponse convertToSnippetResponse(Snippet snippet) {
        return new SnippetResponse(
                snippet.getId(),
                snippet.getTitle(),
                snippet.getDescription(),
                snippet.getContent(),
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

    /**
     * Convert Snippet entity to SnippetSummaryResponse DTO
     */
    private SnippetSummaryResponse convertToSnippetSummaryResponse(Snippet snippet) {
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

    /**
     * Convert Page<Snippet> to PagedResponse<SnippetSummaryResponse>
     */
    private PagedResponse<SnippetSummaryResponse> convertToPagedSummaryResponse(Page<Snippet> snippetPage) {
        List<SnippetSummaryResponse> content = snippetPage.getContent().stream()
                .map(this::convertToSnippetSummaryResponse)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                content,
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
}