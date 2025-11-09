package com.snipper.repository;

import com.snipper.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by username
     * @param username the username to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email
     * @param email the email to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Find active user by username
     * @param username the username to search for
     * @return Optional containing the active user if found
     */
    Optional<User> findByUsernameAndIsActiveTrue(String username);

    /**
     * Find active user by email
     * @param email the email to search for
     * @return Optional containing the active user if found
     */
    Optional<User> findByEmailAndIsActiveTrue(String email);

    /**
     * Check if username exists
     * @param username the username to check
     * @return true if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     * @param email the email to check
     * @return true if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Check if username exists for a different user (for updates)
     * @param username the username to check
     * @param userId the current user's ID to exclude
     * @return true if username exists for another user
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.username = :username AND u.id != :userId")
    boolean existsByUsernameAndIdNot(@Param("username") String username, @Param("userId") Long userId);

    /**
     * Check if email exists for a different user (for updates)
     * @param email the email to check
     * @param userId the current user's ID to exclude
     * @return true if email exists for another user
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.id != :userId")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("userId") Long userId);

    /**
     * Get user statistics including snippet count
     * @param userId the user ID
     * @return array containing [snippetCount, totalViews]
     */
    @Query("SELECT COUNT(s), COALESCE(SUM(s.viewCount), 0) FROM User u LEFT JOIN u.snippets s WHERE u.id = :userId")
    Object[] getUserStatistics(@Param("userId") Long userId);
}