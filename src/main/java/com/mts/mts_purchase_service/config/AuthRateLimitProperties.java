package com.mts.mts_purchase_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configurable rate limit settings for auth-sensitive endpoints.
 */
@Component
@ConfigurationProperties(prefix = "auth.rate-limit")
public class AuthRateLimitProperties {

    private boolean enabled = true;
    private int maxRequests = 5;
    private int windowSeconds = 30;

    /** Returns true when auth endpoint rate limiting is enabled. */
    public boolean isEnabled() {
        return enabled;
    }

    /** Enables/disables auth endpoint rate limiting. */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /** Returns max allowed requests per sliding window. */
    public int getMaxRequests() {
        return maxRequests;
    }

    /** Assigns max allowed requests per sliding window. */
    public void setMaxRequests(int maxRequests) {
        this.maxRequests = maxRequests;
    }

    /** Returns sliding window size in seconds. */
    public int getWindowSeconds() {
        return windowSeconds;
    }

    /** Assigns sliding window size in seconds. */
    public void setWindowSeconds(int windowSeconds) {
        this.windowSeconds = windowSeconds;
    }
}
