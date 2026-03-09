package com.mts.mts_purchase_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configurable settings for OTP-backed user registration.
 */
@Component
@ConfigurationProperties(prefix = "auth.registration.otp")
public class AuthRegistrationOtpProperties {

    private boolean enabled = true;
    private int ttlMinutes = 10;
    private int maxAttempts = 5;
    private boolean devLogEnabled = false;
    private String fromEmail;
    private String ownerEmail;

    /** Returns true when OTP registration flow is enabled. */
    public boolean isEnabled() {
        return enabled;
    }

    /** Enables/disables OTP registration flow. */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /** Returns OTP time-to-live in minutes. */
    public int getTtlMinutes() {
        return ttlMinutes;
    }

    /** Assigns OTP time-to-live in minutes. */
    public void setTtlMinutes(int ttlMinutes) {
        this.ttlMinutes = ttlMinutes;
    }

    /** Returns max invalid OTP attempts allowed. */
    public int getMaxAttempts() {
        return maxAttempts;
    }

    /** Assigns max invalid OTP attempts allowed. */
    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    /** Returns true when raw OTP should be logged in dev. */
    public boolean isDevLogEnabled() {
        return devLogEnabled;
    }

    /** Enables/disables raw OTP logging in dev. */
    public void setDevLogEnabled(boolean devLogEnabled) {
        this.devLogEnabled = devLogEnabled;
    }

    /** Returns sender email configured for OTP notifications. */
    public String getFromEmail() {
        return fromEmail;
    }

    /** Assigns sender email configured for OTP notifications. */
    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }

    /** Returns owner email configured for OTP verification approval. */
    public String getOwnerEmail() {
        return ownerEmail;
    }

    /** Assigns owner email configured for OTP verification approval. */
    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }
}
