package com.mts.mts_purchase_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Session token table to support login expiry and logout invalidation.
 */
@Entity
@Table(name = "USER_SESSIONS")
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SESSION_ID")
    private Long sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private AppUser user;

    @Column(name = "TOKEN_HASH", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "ISSUED_AT", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "EXPIRES_AT", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "LAST_ACCESSED_AT", nullable = false)
    private LocalDateTime lastAccessedAt;

    @Column(name = "IS_REVOKED", nullable = false)
    private int isRevoked = 0;

    /** Returns session id. */
    public Long getSessionId() {
        return sessionId;
    }

    /** Assigns session id. */
    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    /** Returns linked user. */
    public AppUser getUser() {
        return user;
    }

    /** Assigns linked user. */
    public void setUser(AppUser user) {
        this.user = user;
    }

    /** Returns token hash. */
    public String getTokenHash() {
        return tokenHash;
    }

    /** Assigns token hash. */
    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    /** Returns issue timestamp. */
    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    /** Assigns issue timestamp. */
    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }

    /** Returns expiry timestamp. */
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    /** Assigns expiry timestamp. */
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    /** Returns last access timestamp. */
    public LocalDateTime getLastAccessedAt() {
        return lastAccessedAt;
    }

    /** Assigns last access timestamp. */
    public void setLastAccessedAt(LocalDateTime lastAccessedAt) {
        this.lastAccessedAt = lastAccessedAt;
    }

    /** Returns revoked state as boolean. */
    public boolean isRevoked() {
        return isRevoked == 1;
    }

    /** Assigns revoked state. */
    public void setRevoked(boolean revoked) {
        this.isRevoked = revoked ? 1 : 0;
    }

    /** Returns raw revoked flag value. */
    public int getIsRevoked() {
        return isRevoked;
    }

    /** Assigns raw revoked flag value. */
    public void setIsRevoked(int isRevoked) {
        this.isRevoked = isRevoked;
    }
}
