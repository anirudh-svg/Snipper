package com.snipper.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for user dashboard with statistics
 */
public class UserDashboardResponse {

    private UserProfileResponse profile;
    private UserStatistics statistics;
    private List<String> recentLanguages;

    // Default constructor
    public UserDashboardResponse() {}

    // Constructor with all fields
    public UserDashboardResponse(UserProfileResponse profile, UserStatistics statistics, List<String> recentLanguages) {
        this.profile = profile;
        this.statistics = statistics;
        this.recentLanguages = recentLanguages;
    }

    // Getters and Setters
    public UserProfileResponse getProfile() {
        return profile;
    }

    public void setProfile(UserProfileResponse profile) {
        this.profile = profile;
    }

    public UserStatistics getStatistics() {
        return statistics;
    }

    public void setStatistics(UserStatistics statistics) {
        this.statistics = statistics;
    }

    public List<String> getRecentLanguages() {
        return recentLanguages;
    }

    public void setRecentLanguages(List<String> recentLanguages) {
        this.recentLanguages = recentLanguages;
    }

    /**
     * Inner class for user statistics
     */
    public static class UserStatistics {
        private long totalSnippets;
        private long publicSnippets;
        private long privateSnippets;
        private long unlistedSnippets;
        private long totalViews;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime lastActivity;

        // Default constructor
        public UserStatistics() {}

        // Constructor with all fields
        public UserStatistics(long totalSnippets, long publicSnippets, long privateSnippets, 
                             long unlistedSnippets, long totalViews, LocalDateTime lastActivity) {
            this.totalSnippets = totalSnippets;
            this.publicSnippets = publicSnippets;
            this.privateSnippets = privateSnippets;
            this.unlistedSnippets = unlistedSnippets;
            this.totalViews = totalViews;
            this.lastActivity = lastActivity;
        }

        // Getters and Setters
        public long getTotalSnippets() {
            return totalSnippets;
        }

        public void setTotalSnippets(long totalSnippets) {
            this.totalSnippets = totalSnippets;
        }

        public long getPublicSnippets() {
            return publicSnippets;
        }

        public void setPublicSnippets(long publicSnippets) {
            this.publicSnippets = publicSnippets;
        }

        public long getPrivateSnippets() {
            return privateSnippets;
        }

        public void setPrivateSnippets(long privateSnippets) {
            this.privateSnippets = privateSnippets;
        }

        public long getUnlistedSnippets() {
            return unlistedSnippets;
        }

        public void setUnlistedSnippets(long unlistedSnippets) {
            this.unlistedSnippets = unlistedSnippets;
        }

        public long getTotalViews() {
            return totalViews;
        }

        public void setTotalViews(long totalViews) {
            this.totalViews = totalViews;
        }

        public LocalDateTime getLastActivity() {
            return lastActivity;
        }

        public void setLastActivity(LocalDateTime lastActivity) {
            this.lastActivity = lastActivity;
        }
    }
}