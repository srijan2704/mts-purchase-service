package com.mts.mts_purchase_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Application user credentials entity.
 */
@Entity
@Table(name = "APP_USERS")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "USERNAME", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "PASSWORD_HASH", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "IS_ACTIVE", nullable = false)
    private int isActive = 1;

    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    /** Initializes audit columns before initial persist. */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        this.isActive = 1;
    }

    /** Updates timestamp before updates. */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /** Returns user id. */
    public Long getUserId() {
        return userId;
    }

    /** Assigns user id. */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /** Returns username. */
    public String getUsername() {
        return username;
    }

    /** Assigns username. */
    public void setUsername(String username) {
        this.username = username;
    }

    /** Returns hashed password string. */
    public String getPasswordHash() {
        return passwordHash;
    }

    /** Assigns hashed password string. */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /** Returns active state as boolean. */
    public boolean isActive() {
        return isActive == 1;
    }

    /** Assigns active state. */
    public void setActive(boolean active) {
        this.isActive = active ? 1 : 0;
    }

    /** Returns raw active flag value. */
    public int getIsActive() {
        return isActive;
    }

    /** Assigns raw active flag value. */
    public void setIsActive(int isActive) {
        this.isActive = isActive;
    }

    /** Returns creation timestamp. */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** Assigns creation timestamp. */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /** Returns update timestamp. */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /** Assigns update timestamp. */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
