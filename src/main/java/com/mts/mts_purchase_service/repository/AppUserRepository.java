package com.mts.mts_purchase_service.repository;

import com.mts.mts_purchase_service.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for application user credentials.
 */
@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    /** Returns one user by username ignoring case. */
    Optional<AppUser> findByUsernameIgnoreCase(String username);
}
