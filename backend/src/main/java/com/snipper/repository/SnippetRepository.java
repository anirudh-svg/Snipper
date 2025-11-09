package com.snipper.repository;

import com.snipper.model.Snippet;
import com.snipper.model.User;
import com.snipper.model.VisibilityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SnippetRepository extends JpaRepository<Snippet, Long> {

    /**
     * Find all public snippets ordered by creation date
     * @param pageable pagination information
     * @return page of public snippets
     */
    Page<Snippet> findByVisibilityOrderByCreatedAtDesc(VisibilityType visibility, Pageable pageable);

    /**
     * Find all snippets by author
     * @param author the author
     * @param pageable pagination information
     * @return page of author's snippets
     */
    Page<Snippet> findByAuthorOrderByCreatedAtDesc(User author, Pageable pageable);

    /**
     * Find snippets by author and visibility
     * @param author the author
     * @param visibility the visibility type
     * @param pageable pagination information
     * @return page of filtered snippets
     */
    Page<Snippet> findByAuthorAndVisibilityOrderByCreatedAtDesc(User author, VisibilityType visibility, Pageable pageable);

    /**
     * Find snippets by language and visibility
     * @param language the programming language
     * @param visibility the visibility type
     * @param pageable pagination information
     * @return page of filtered snippets
     */
    Page<Snippet> findByLanguageAndVisibilityOrderByCreatedAtDesc(String language, VisibilityType visibility, Pageable pageable);

    /**
     * Find snippets by language (public only)
     * @param language the programming language
     * @param pageable pagination information
     * @return page of public snippets in the specified language
     */
    @Query("SELECT s FROM Snippet s WHERE s.language = :language AND s.visibility = 'PUBLIC' ORDER BY s.createdAt DESC")
    Page<Snippet> findPublicSnippetsByLanguage(@Param("language") String language, Pageable pageable);

    /**
     * Search snippets using text search
     * @param searchTerm the search term
     * @param pageable pagination information
     * @return page of matching public snippets
     */
    @Query("SELECT s FROM Snippet s WHERE s.visibility = 'PUBLIC' AND " +
           "(s.title LIKE %:searchTerm% " +
           "OR s.description LIKE %:searchTerm% " +
           "OR s.content LIKE %:searchTerm% " +
           "OR s.tags LIKE %:searchTerm%) " +
           "ORDER BY s.createdAt DESC")
    Page<Snippet> searchPublicSnippets(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Search user's snippets
     * @param searchTerm the search term
     * @param author the author
     * @param pageable pagination information
     * @return page of matching user snippets
     */
    @Query("SELECT s FROM Snippet s WHERE s.author = :author AND " +
           "(s.title LIKE %:searchTerm% " +
           "OR s.description LIKE %:searchTerm% " +
           "OR s.tags LIKE %:searchTerm% " +
           "OR s.content LIKE %:searchTerm%) " +
           "ORDER BY s.createdAt DESC")
    Page<Snippet> searchUserSnippets(@Param("searchTerm") String searchTerm, @Param("author") User author, Pageable pageable);

    /**
     * Find snippets containing specific tags
     * @param tag the tag to search for
     * @param visibility the visibility type
     * @param pageable pagination information
     * @return page of snippets with the tag
     */
    @Query("SELECT s FROM Snippet s WHERE s.visibility = :visibility AND s.tags LIKE %:tag% ORDER BY s.createdAt DESC")
    Page<Snippet> findByTagsContainingAndVisibility(@Param("tag") String tag, @Param("visibility") VisibilityType visibility, Pageable pageable);

    /**
     * Get most popular public snippets
     * @param pageable pagination information
     * @return page of popular snippets ordered by view count
     */
    @Query("SELECT s FROM Snippet s WHERE s.visibility = 'PUBLIC' ORDER BY s.viewCount DESC, s.createdAt DESC")
    Page<Snippet> findMostPopularPublicSnippets(Pageable pageable);

    /**
     * Get recent public snippets
     * @param pageable pagination information
     * @return page of recent public snippets
     */
    @Query("SELECT s FROM Snippet s WHERE s.visibility = 'PUBLIC' ORDER BY s.createdAt DESC")
    Page<Snippet> findRecentPublicSnippets(Pageable pageable);

    /**
     * Get distinct languages from public snippets
     * @return list of programming languages
     */
    @Query("SELECT DISTINCT s.language FROM Snippet s WHERE s.visibility = 'PUBLIC' ORDER BY s.language")
    List<String> findDistinctLanguagesFromPublicSnippets();

    /**
     * Get distinct languages from user's snippets
     * @param author the author
     * @return list of programming languages used by the user
     */
    @Query("SELECT DISTINCT s.language FROM Snippet s WHERE s.author = :author ORDER BY s.language")
    List<String> findDistinctLanguagesByAuthor(@Param("author") User author);

    /**
     * Count snippets by author
     * @param author the author
     * @return number of snippets
     */
    long countByAuthor(User author);

    /**
     * Count public snippets by author
     * @param author the author
     * @return number of public snippets
     */
    long countByAuthorAndVisibility(User author, VisibilityType visibility);

    /**
     * Increment view count for a snippet
     * @param snippetId the snippet ID
     */
    @Modifying
    @Query("UPDATE Snippet s SET s.viewCount = s.viewCount + 1 WHERE s.id = :snippetId")
    void incrementViewCount(@Param("snippetId") Long snippetId);

    /**
     * Find snippet by ID and author (for ownership verification)
     * @param id the snippet ID
     * @param author the author
     * @return optional snippet if found and owned by author
     */
    Optional<Snippet> findByIdAndAuthor(Long id, User author);

    /**
     * Find public or unlisted snippet by ID (for public access)
     * @param id the snippet ID
     * @return optional snippet if found and publicly accessible
     */
    @Query("SELECT s FROM Snippet s WHERE s.id = :id AND s.visibility IN ('PUBLIC', 'UNLISTED')")
    Optional<Snippet> findPublicOrUnlistedById(@Param("id") Long id);

    /**
     * Get snippet statistics for a user
     * @param author the author
     * @return array containing [totalSnippets, publicSnippets, privateSnippets, unlistedSnippets, totalViews]
     */
    @Query("SELECT " +
           "COUNT(s), " +
           "SUM(CASE WHEN s.visibility = 'PUBLIC' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN s.visibility = 'PRIVATE' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN s.visibility = 'UNLISTED' THEN 1 ELSE 0 END), " +
           "COALESCE(SUM(s.viewCount), 0) " +
           "FROM Snippet s WHERE s.author = :author")
    Object[] getSnippetStatisticsByAuthor(@Param("author") User author);

    /**
     * Get distinct tags from public snippets
     * @return list of unique tags (comma-separated values will need to be processed in service)
     */
    @Query("SELECT DISTINCT s.tags FROM Snippet s WHERE s.visibility = 'PUBLIC' AND s.tags IS NOT NULL AND s.tags != '' ORDER BY s.tags")
    List<String> findDistinctTagsFromPublicSnippets();

    /**
     * Search snippets with advanced filtering (public snippets only)
     * @param searchTerm the search term (optional)
     * @param language the programming language filter (optional)
     * @param tags the tags filter (optional)
     * @param pageable pagination information
     * @return page of filtered snippets
     */
    @Query("SELECT s FROM Snippet s WHERE " +
           "s.visibility = 'PUBLIC' AND " +
           "(:searchTerm IS NULL OR " +
           " s.title LIKE %:searchTerm% OR " +
           " s.description LIKE %:searchTerm% OR " +
           " s.content LIKE %:searchTerm% OR " +
           " s.tags LIKE %:searchTerm%) AND " +
           "(:language IS NULL OR s.language = :language) AND " +
           "(:tags IS NULL OR s.tags LIKE %:tags%) " +
           "ORDER BY s.createdAt DESC")
    Page<Snippet> searchSnippetsWithFilters(
            @Param("searchTerm") String searchTerm,
            @Param("language") String language,
            @Param("tags") String tags,
            Pageable pageable);

    /**
     * Search user's snippets with advanced filtering
     * @param searchTerm the search term (optional)
     * @param language the programming language filter (optional)
     * @param tags the tags filter (optional)
     * @param visibility the visibility filter (optional)
     * @param author the author
     * @param pageable pagination information
     * @return page of filtered user snippets
     */
    @Query("SELECT s FROM Snippet s WHERE " +
           "s.author = :author AND " +
           "(:searchTerm IS NULL OR " +
           " s.title LIKE %:searchTerm% OR " +
           " s.description LIKE %:searchTerm% OR " +
           " s.content LIKE %:searchTerm% OR " +
           " s.tags LIKE %:searchTerm%) AND " +
           "(:language IS NULL OR s.language = :language) AND " +
           "(:tags IS NULL OR s.tags LIKE %:tags%) AND " +
           "(:visibility IS NULL OR s.visibility = :visibility) " +
           "ORDER BY s.createdAt DESC")
    Page<Snippet> searchUserSnippetsWithFilters(
            @Param("searchTerm") String searchTerm,
            @Param("language") String language,
            @Param("tags") String tags,
            @Param("visibility") String visibility,
            @Param("author") User author,
            Pageable pageable);
}