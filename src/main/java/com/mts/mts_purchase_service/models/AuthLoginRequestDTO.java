package com.mts.mts_purchase_service.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Login request payload.
 */
public class AuthLoginRequestDTO {

    @NotBlank(message = "Username is required")
    @Size(max = 100, message = "Username must not exceed 100 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    /** Returns username. */
    public String getUsername() {
        return username;
    }

    /** Assigns username. */
    public void setUsername(String username) {
        this.username = username;
    }

    /** Returns password. */
    public String getPassword() {
        return password;
    }

    /** Assigns password. */
    public void setPassword(String password) {
        this.password = password;
    }
}
