package com.snipper.dto.snippet;

import com.snipper.model.VisibilityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateSnippetRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotBlank(message = "Content is required")
    private String content;

    @NotBlank(message = "Language is required")
    @Size(max = 50, message = "Language must not exceed 50 characters")
    private String language;

    @Size(max = 500, message = "Tags must not exceed 500 characters")
    private String tags;

    @NotNull(message = "Visibility is required")
    private VisibilityType visibility = VisibilityType.PUBLIC;

    // Default constructor
    public CreateSnippetRequest() {}

    // Constructor
    public CreateSnippetRequest(String title, String description, String content, String language, String tags, VisibilityType visibility) {
        this.title = title;
        this.description = description;
        this.content = content;
        this.language = language;
        this.tags = tags;
        this.visibility = visibility;
    }

    // Getters and Setters
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
}