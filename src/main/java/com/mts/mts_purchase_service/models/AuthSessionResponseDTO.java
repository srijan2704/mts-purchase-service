package com.mts.mts_purchase_service.models;

import java.time.LocalDateTime;

/**
 * Login session response payload.
 */
public class AuthSessionResponseDTO {

    private String username;
    private String token;
    private LocalDateTime expiresAt;

    /** Returns username. */
    public String getUsername() {
        return username;
    }

    /** Assigns username. */
    public void setUsername(String username) {
        this.username = username;
    }

    /** Returns bearer token. */
    public String getToken() {
        return token;
    }

    /** Assigns bearer token. */
    public void setToken(String token) {
        this.token = token;
    }

    /** Returns token expiry time. */
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    /** Assigns token expiry time. */
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
