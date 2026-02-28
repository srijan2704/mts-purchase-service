package com.mts.mts_purchase_service.repository;

import com.mts.mts_purchase_service.entity.UserRegistrationOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for pending registration OTP requests.
 */
@Repository
public interface UserRegistrationOtpRepository extends JpaRepository<UserRegistrationOtp, Long> {

    /** Returns latest active OTP request for a username. */
    Optional<UserRegistrationOtp> findTopByUsernameIgnoreCaseAndIsVerifiedAndConsumedAtIsNullOrderByCreatedAtDesc(
            String username,
            int isVerified
    );

    /** Deletes active pending OTP requests for username before issuing fresh OTP. */
    @Modifying
    @Query("DELETE FROM UserRegistrationOtp o WHERE LOWER(o.username) = LOWER(:username) AND o.isVerified = 0 AND o.consumedAt IS NULL")
    int deleteActivePendingByUsername(@Param("username") String username);
}
