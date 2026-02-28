package com.mts.mts_purchase_service.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * OTP verification payload to complete new-user registration.
 */
public class AuthRegisterOtpVerifyRequestDTO {

    @NotBlank(message = "Username is required")
    @Size(max = 100, message = "Username must not exceed 100 characters")
    private String username;

    @NotBlank(message = "OTP is required")
    @Pattern(regexp = "\\d{6}", message = "OTP must be 6 digits")
    private String otp;

    /** Returns username for pending registration. */
    public String getUsername() {
        return username;
    }

    /** Assigns username for pending registration. */
    public void setUsername(String username) {
        this.username = username;
    }

    /** Returns OTP entered by user. */
    public String getOtp() {
        return otp;
    }

    /** Assigns OTP entered by user. */
    public void setOtp(String otp) {
        this.otp = otp;
    }
}
