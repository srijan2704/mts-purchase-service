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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AuthServiceImpl session and credential rules.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private UserSessionRepository userSessionRepository;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(appUserRepository, userSessionRepository, new BCryptPasswordEncoder());
    }

    @Test
    void setupInitialCredential_shouldSaveWhenNoUserExists() {
        AuthSetupRequestDTO request = new AuthSetupRequestDTO();
        request.setUsername("owner");
        request.setPassword("StrongPass123");

        when(appUserRepository.count()).thenReturn(0L);

        authService.setupInitialCredential(request);

        verify(appUserRepository).save(any(AppUser.class));
    }

    @Test
    void login_shouldCreateSessionWhenCredentialValid() {
        AppUser user = new AppUser();
        user.setUserId(1L);
        user.setUsername("owner");
        user.setActive(true);
        user.setPasswordHash(new BCryptPasswordEncoder().encode("StrongPass123"));

        AuthLoginRequestDTO request = new AuthLoginRequestDTO();
        request.setUsername("owner");
        request.setPassword("StrongPass123");

        when(appUserRepository.findByUsernameIgnoreCase("owner")).thenReturn(Optional.of(user));

        AuthSessionResponseDTO response = authService.login(request);

        assertEquals("owner", response.getUsername());
        assertNotNull(response.getToken());
        assertNotNull(response.getExpiresAt());
        verify(userSessionRepository).revokeActiveSessionsByUserId(1L);
        verify(userSessionRepository).save(any(UserSession.class));
    }

    @Test
    void validateAndRefresh_shouldThrowWhenExpired() {
        AppUser user = new AppUser();
        user.setUserId(1L);
        user.setUsername("owner");
        user.setActive(true);

        String rawToken = "token123";
        String tokenHash = TokenUtils.sha256Hex(rawToken);

        UserSession session = new UserSession();
        session.setUser(user);
        session.setTokenHash(tokenHash);
        session.setRevoked(false);
        session.setIssuedAt(LocalDateTime.now().minusMinutes(20));
        session.setLastAccessedAt(LocalDateTime.now().minusMinutes(20));
        session.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        when(userSessionRepository.findByTokenHashAndIsRevoked(tokenHash, 0)).thenReturn(Optional.of(session));

        assertThrows(UnauthorizedException.class, () -> authService.validateAndRefresh(rawToken));
    }

    @Test
    void validateAndRefresh_shouldReturnInfoWhenSessionValid() {
        AppUser user = new AppUser();
        user.setUserId(1L);
        user.setUsername("owner");
        user.setActive(true);

        String rawToken = "token456";
        String tokenHash = TokenUtils.sha256Hex(rawToken);

        UserSession session = new UserSession();
        session.setUser(user);
        session.setTokenHash(tokenHash);
        session.setRevoked(false);
        session.setIssuedAt(LocalDateTime.now().minusMinutes(1));
        session.setLastAccessedAt(LocalDateTime.now().minusMinutes(1));
        session.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        when(userSessionRepository.findByTokenHashAndIsRevoked(tokenHash, 0)).thenReturn(Optional.of(session));

        AuthSessionInfoDTO info = authService.validateAndRefresh(rawToken);

        assertEquals("owner", info.getUsername());
        assertNotNull(info.getExpiresAt());
        verify(userSessionRepository).save(any(UserSession.class));
    }
}
