package com.mts.mts_purchase_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Pending OTP-backed registration request for creating new application users.
 */
@Entity
@Table(name = "USER_REGISTRATION_OTPS")
public class UserRegistrationOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OTP_ID")
    private Long otpId;

    @Column(name = "USERNAME", nullable = false, length = 100)
    private String username;

    @Column(name = "PASSWORD_HASH", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "OTP_HASH", nullable = false, length = 64)
    private String otpHash;

    @Column(name = "OWNER_EMAIL", nullable = false, length = 150)
    private String ownerEmail;

    @Column(name = "EXPIRES_AT", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "ATTEMPTS_LEFT", nullable = false)
    private int attemptsLeft;

    @Column(name = "IS_VERIFIED", nullable = false)
    private int isVerified = 0;

    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "CONSUMED_AT")
    private LocalDateTime consumedAt;

    /** Initializes creation timestamp and default flags. */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (isVerified != 1) {
            isVerified = 0;
        }
    }

    /** Returns OTP request id. */
    public Long getOtpId() {
        return otpId;
    }

    /** Assigns OTP request id. */
    public void setOtpId(Long otpId) {
        this.otpId = otpId;
    }

    /** Returns username for pending registration. */
    public String getUsername() {
        return username;
    }

    /** Assigns username for pending registration. */
    public void setUsername(String username) {
        this.username = username;
    }

    /** Returns hashed password prepared for final user creation. */
    public String getPasswordHash() {
        return passwordHash;
    }

    /** Assigns hashed password prepared for final user creation. */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /** Returns hashed OTP value. */
    public String getOtpHash() {
        return otpHash;
    }

    /** Assigns hashed OTP value. */
    public void setOtpHash(String otpHash) {
        this.otpHash = otpHash;
    }

    /** Returns owner email where OTP is delivered. */
    public String getOwnerEmail() {
        return ownerEmail;
    }

    /** Assigns owner email where OTP is delivered. */
    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    /** Returns OTP expiry timestamp. */
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    /** Assigns OTP expiry timestamp. */
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    /** Returns remaining verification attempts. */
    public int getAttemptsLeft() {
        return attemptsLeft;
    }

    /** Assigns remaining verification attempts. */
    public void setAttemptsLeft(int attemptsLeft) {
        this.attemptsLeft = attemptsLeft;
    }

    /** Returns true when request is verified. */
    public boolean isVerified() {
        return isVerified == 1;
    }

    /** Assigns verified state. */
    public void setVerified(boolean verified) {
        this.isVerified = verified ? 1 : 0;
    }

    /** Returns raw verified flag. */
    public int getIsVerified() {
        return isVerified;
    }

    /** Assigns raw verified flag. */
    public void setIsVerified(int isVerified) {
        this.isVerified = isVerified;
    }

    /** Returns creation timestamp. */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** Assigns creation timestamp. */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /** Returns consumed timestamp once OTP flow completes or expires. */
    public LocalDateTime getConsumedAt() {
        return consumedAt;
    }

    /** Assigns consumed timestamp once OTP flow completes or expires. */
    public void setConsumedAt(LocalDateTime consumedAt) {
        this.consumedAt = consumedAt;
    }
}
