-- Create snippets table
CREATE TABLE snippets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    content LONGTEXT NOT NULL,
    language VARCHAR(50) NOT NULL,
    tags VARCHAR(500),
    visibility ENUM('PUBLIC', 'PRIVATE', 'UNLISTED') NOT NULL DEFAULT 'PUBLIC',
    view_count BIGINT NOT NULL DEFAULT 0,
    author_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key constraint
    CONSTRAINT fk_snippet_author FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX idx_snippet_author ON snippets(author_id);
CREATE INDEX idx_snippet_visibility ON snippets(visibility);
CREATE INDEX idx_snippet_language ON snippets(language);
CREATE INDEX idx_snippet_created_at ON snippets(created_at);
CREATE INDEX idx_snippet_title ON snippets(title);
CREATE INDEX idx_snippet_tags ON snippets(tags);
CREATE INDEX idx_snippet_view_count ON snippets(view_count);

-- Create composite indexes for common query patterns
CREATE INDEX idx_snippet_visibility_created_at ON snippets(visibility, created_at DESC);
CREATE INDEX idx_snippet_author_visibility ON snippets(author_id, visibility);
CREATE INDEX idx_snippet_language_visibility ON snippets(language, visibility);

-- Create full-text search index for content and title
CREATE FULLTEXT INDEX idx_snippet_search ON snippets(title, description, content, tags);