package com.mts.mts_purchase_service.repository;

import com.mts.mts_purchase_service.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for token-based user sessions.
 */
@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    /** Returns one active session by token hash. */
    Optional<UserSession> findByTokenHashAndIsRevoked(String tokenHash, int isRevoked);

    /** Revokes all active sessions of a specific user. */
    @Modifying
    @Query("UPDATE UserSession s SET s.isRevoked = 1 WHERE s.user.userId = :userId AND s.isRevoked = 0")
    int revokeActiveSessionsByUserId(@Param("userId") Long userId);
}
