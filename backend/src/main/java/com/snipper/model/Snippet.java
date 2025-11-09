package com.snipper.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "snippets", indexes = {
    @Index(name = "idx_snippet_author", columnList = "author_id"),
    @Index(name = "idx_snippet_visibility", columnList = "visibility"),
    @Index(name = "idx_snippet_language", columnList = "language"),
    @Index(name = "idx_snippet_created_at", columnList = "created_at"),
    @Index(name = "idx_snippet_title", columnList = "title"),
    @Index(name = "idx_snippet_tags", columnList = "tags")
})
public class Snippet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    @Column(nullable = false, length = 200)
    private String title;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Column(columnDefinition = "TEXT")
    private String description;

    @NotBlank(message = "Content is required")
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String content;

    @NotBlank(message = "Language is required")
    @Size(max = 50, message = "Language must not exceed 50 characters")
    @Column(nullable = false, length = 50)
    private String language;

    @Size(max = 500, message = "Tags must not exceed 500 characters")
    @Column(length = 500)
    private String tags;

    @NotNull(message = "Visibility is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VisibilityType visibility = VisibilityType.PUBLIC;

    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @NotNull(message = "Author is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false, foreignKey = @ForeignKey(name = "fk_snippet_author"))
    private User author;

    // Default constructor
    public Snippet() {}

    // Constructor for creating new snippets
    public Snippet(String title, String content, String language, VisibilityType visibility, User author) {
        this.title = title;
        this.content = content;
        this.language = language;
        this.visibility = visibility;
        this.author = author;
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

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    /**
     * Increment view count by 1
     */
    public void incrementViewCount() {
        this.viewCount++;
    }

    /**
     * Check if the snippet is owned by the given user
     * @param user the user to check
     * @return true if the user owns this snippet
     */
    public boolean isOwnedBy(User user) {
        return user != null && user.getId() != null && user.getId().equals(this.author.getId());
    }

    /**
     * Check if the snippet is publicly visible
     * @return true if visibility is PUBLIC
     */
    public boolean isPublic() {
        return VisibilityType.PUBLIC.equals(this.visibility);
    }

    /**
     * Check if the snippet is private
     * @return true if visibility is PRIVATE
     */
    public boolean isPrivate() {
        return VisibilityType.PRIVATE.equals(this.visibility);
    }

    /**
     * Check if the snippet is unlisted
     * @return true if visibility is UNLISTED
     */
    public boolean isUnlisted() {
        return VisibilityType.UNLISTED.equals(this.visibility);
    }

    @Override
    public String toString() {
        return "Snippet{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", language='" + language + '\'' +
                ", visibility=" + visibility +
                ", viewCount=" + viewCount +
                ", createdAt=" + createdAt +
                ", authorId=" + (author != null ? author.getId() : null) +
                '}';
    }
}