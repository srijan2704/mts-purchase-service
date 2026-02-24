package com.mts.mts_purchase_service.models;

import java.time.LocalDateTime;

/**
 * Authenticated session info payload.
 */
public class AuthSessionInfoDTO {

    private String username;
    private LocalDateTime expiresAt;

    /** Returns username. */
    public String getUsername() {
        return username;
    }

    /** Assigns username. */
    public void setUsername(String username) {
        this.username = username;
    }

    /** Returns session expiry time. */
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    /** Assigns session expiry time. */
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
