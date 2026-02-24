package com.mts.mts_purchase_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Security-related bean configuration.
 */
@Configuration
public class SecurityBeansConfig {

    /** Provides BCrypt hasher for password storage and verification. */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
