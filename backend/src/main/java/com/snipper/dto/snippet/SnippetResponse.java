package com.snipper.dto.snippet;

import com.snipper.model.VisibilityType;
import java.time.LocalDateTime;

public class SnippetResponse {

    private Long id;
    private String title;
    private String description;
    private String content;
    private String language;
    private String tags;
    private VisibilityType visibility;
    private Long viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String authorUsername;
    private Long authorId;

    // Default constructor
    public SnippetResponse() {}

    // Constructor
    public SnippetResponse(Long id, String title, String description, String content, String language, 
                          String tags, VisibilityType visibility, Long viewCount, LocalDateTime createdAt, 
                          LocalDateTime updatedAt, String authorUsername, Long authorId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.content = content;
        this.language = language;
        this.tags = tags;
        this.visibility = visibility;
        this.viewCount = viewCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.authorUsername = authorUsername;
        this.authorId = authorId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public VisibilityType getVisibility() {
        return visibility;
    }

    public void setVisibility(VisibilityType visibility) {
        this.visibility = visibility;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }
}