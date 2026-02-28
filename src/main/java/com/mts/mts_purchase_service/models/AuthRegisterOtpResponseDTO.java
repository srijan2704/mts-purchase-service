package com.mts.mts_purchase_service.models;

import java.time.LocalDateTime;

/**
 * Response payload after OTP request creation.
 */
public class AuthRegisterOtpResponseDTO {

    private String username;
    private String deliveryEmailMasked;
    private LocalDateTime expiresAt;

    /** Returns username associated with OTP request. */
    public String getUsername() {
        return username;
    }

    /** Assigns username associated with OTP request. */
    public void setUsername(String username) {
        this.username = username;
    }

    /** Returns masked email where OTP is delivered. */
    public String getDeliveryEmailMasked() {
        return deliveryEmailMasked;
    }

    /** Assigns masked email where OTP is delivered. */
    public void setDeliveryEmailMasked(String deliveryEmailMasked) {
        this.deliveryEmailMasked = deliveryEmailMasked;
    }

    /** Returns OTP expiry timestamp. */
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    /** Assigns OTP expiry timestamp. */
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
