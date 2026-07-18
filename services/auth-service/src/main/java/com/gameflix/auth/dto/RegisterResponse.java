package com.gameflix.auth.dto;

import java.time.Instant;

public class RegisterResponse {

    private final Long userId;
    private final String username;
    private final String email;
    private final String displayName;
    private final Instant createdAt;

    public RegisterResponse(Long userId, String username, String email, String displayName, Instant createdAt) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.displayName = displayName;
        this.createdAt = createdAt;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
