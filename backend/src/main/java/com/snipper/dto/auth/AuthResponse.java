package com.snipper.dto.auth;

public class AuthResponse {
    private String token;
    private String refreshToken;
    private String tokenType = "Bearer";
    private UserInfo user;

    public AuthResponse() {}

    public AuthResponse(String token, String refreshToken, Long userId, String username, String email) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.user = new UserInfo(userId, username, email);
    }

    public static class UserInfo {
        private Long id;
        private String username;
        private String email;

        public UserInfo() {}

        public UserInfo(Long id, String username, String email) {
            this.id = id;
            this.username = username;
            this.email = email;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }
}