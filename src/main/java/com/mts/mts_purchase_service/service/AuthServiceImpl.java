package com.mts.mts_purchase_service.service;

import com.mts.mts_purchase_service.entity.AppUser;
import com.mts.mts_purchase_service.entity.UserSession;
import com.mts.mts_purchase_service.exception.UnauthorizedException;
import com.mts.mts_purchase_service.models.AuthLoginRequestDTO;
import com.mts.mts_purchase_service.models.AuthSessionInfoDTO;
import com.mts.mts_purchase_service.models.AuthSessionResponseDTO;
import com.mts.mts_purchase_service.models.AuthSetupRequestDTO;
import com.mts.mts_purchase_service.repository.AppUserRepository;
import com.mts.mts_purchase_service.repository.UserSessionRepository;
import com.mts.mts_purchase_service.util.TokenUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Auth service implementation using DB-backed token sessions.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final int SESSION_TTL_MINUTES = 15;

    private final AppUserRepository appUserRepository;
    private final UserSessionRepository userSessionRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthServiceImpl(AppUserRepository appUserRepository,
                           UserSessionRepository userSessionRepository,
                           BCryptPasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.userSessionRepository = userSessionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Allows initial bootstrap user creation only once.
     */
    @Override
    @Transactional
    public void setupInitialCredential(AuthSetupRequestDTO request) {
        if (appUserRepository.count() > 0) {
            throw new IllegalStateException("Credentials are already configured");
        }

        AppUser user = new AppUser();
        user.setUsername(request.getUsername().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setActive(true);
        appUserRepository.save(user);
    }

    /**
     * Creates a new token session after credential validation.
     */
    @Override
    @Transactional
    public AuthSessionResponseDTO login(AuthLoginRequestDTO request) {
        AppUser user = appUserRepository.findByUsernameIgnoreCase(request.getUsername().trim())
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        if (!user.isActive()) {
            throw new UnauthorizedException("User account is inactive");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid username or password");
        }

        // Single-session policy: revoke previously active sessions.
        userSessionRepository.revokeActiveSessionsByUserId(user.getUserId());

        String rawToken = TokenUtils.generateSecureToken();
        String tokenHash = TokenUtils.sha256Hex(rawToken);

        LocalDateTime now = LocalDateTime.now();
        UserSession session = new UserSession();
        session.setUser(user);
        session.setTokenHash(tokenHash);
        session.setIssuedAt(now);
        session.setLastAccessedAt(now);
        session.setExpiresAt(now.plusMinutes(SESSION_TTL_MINUTES));
        session.setRevoked(false);
        userSessionRepository.save(session);

        AuthSessionResponseDTO response = new AuthSessionResponseDTO();
        response.setUsername(user.getUsername());
        response.setToken(rawToken);
        response.setExpiresAt(session.getExpiresAt());
        return response;
    }

    /**
     * Validates token and extends expiry by 15 minutes (sliding expiry).
     */
    @Override
    @Transactional
    public AuthSessionInfoDTO validateAndRefresh(String rawToken) {
        UserSession session = getActiveSession(rawToken);

        LocalDateTime now = LocalDateTime.now();
        if (session.getExpiresAt().isBefore(now)) {
            session.setRevoked(true);
            userSessionRepository.save(session);
            throw new UnauthorizedException("Session expired. Please login again.");
        }

        session.setLastAccessedAt(now);
        session.setExpiresAt(now.plusMinutes(SESSION_TTL_MINUTES));
        userSessionRepository.save(session);

        AuthSessionInfoDTO info = new AuthSessionInfoDTO();
        info.setUsername(session.getUser().getUsername());
        info.setExpiresAt(session.getExpiresAt());
        return info;
    }

    /**
     * Revokes the current token session.
     */
    @Override
    @Transactional
    public void logout(String rawToken) {
        UserSession session = getActiveSession(rawToken);
        session.setRevoked(true);
        userSessionRepository.save(session);
    }

    /**
     * Finds one non-revoked session by token hash.
     */
    private UserSession getActiveSession(String rawToken) {
        String tokenHash = TokenUtils.sha256Hex(rawToken);
        return userSessionRepository.findByTokenHashAndIsRevoked(tokenHash, 0)
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired session"));
    }
}
