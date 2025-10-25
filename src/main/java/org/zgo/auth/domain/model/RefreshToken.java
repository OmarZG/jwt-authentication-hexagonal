package org.zgo.auth.domain.model;

import java.time.Instant;

public class RefreshToken {
    private Long id;
    private String token;
    private String username;
    private Instant expiresAt;
    private boolean revoked;

    public RefreshToken() {
    }

    public RefreshToken(Long id, String token, String username, Instant expiresAt, boolean revoked) {
        this.id = id;
        this.token = token;
        this.username = username;
        this.expiresAt = expiresAt;
        this.revoked = revoked;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }
}