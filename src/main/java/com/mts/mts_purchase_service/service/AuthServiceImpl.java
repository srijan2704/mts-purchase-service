package com.mts.mts_purchase_service.service;

import com.mts.mts_purchase_service.config.AuthRegistrationOtpProperties;
import com.mts.mts_purchase_service.entity.AppUser;
import com.mts.mts_purchase_service.entity.UserRegistrationOtp;
import com.mts.mts_purchase_service.entity.UserSession;
import com.mts.mts_purchase_service.exception.DuplicateResourceException;
import com.mts.mts_purchase_service.exception.UnauthorizedException;
import com.mts.mts_purchase_service.models.AuthLoginRequestDTO;
import com.mts.mts_purchase_service.models.AuthRegisterOtpResponseDTO;
import com.mts.mts_purchase_service.models.AuthRegisterOtpVerifyRequestDTO;
import com.mts.mts_purchase_service.models.AuthRegisterRequestDTO;
import com.mts.mts_purchase_service.models.AuthSessionInfoDTO;
import com.mts.mts_purchase_service.models.AuthSessionResponseDTO;
import com.mts.mts_purchase_service.models.AuthSetupRequestDTO;
import com.mts.mts_purchase_service.repository.AppUserRepository;
import com.mts.mts_purchase_service.repository.UserRegistrationOtpRepository;
import com.mts.mts_purchase_service.repository.UserSessionRepository;
import com.mts.mts_purchase_service.util.TokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Auth service implementation using DB-backed token sessions.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private static final int SESSION_TTL_MINUTES = 15;
    private static final int OTP_LENGTH = 6;
    private static final SecureRandom OTP_RANDOM = new SecureRandom();
    private static final String ADMIN_REGISTRATION_EMAIL = "bgp.maatarastore@gmail.com";

    private final AppUserRepository appUserRepository;
    private final UserSessionRepository userSessionRepository;
    private final UserRegistrationOtpRepository userRegistrationOtpRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JavaMailSender javaMailSender;
    private final AuthRegistrationOtpProperties otpProperties;
    private final Environment environment;

    public AuthServiceImpl(AppUserRepository appUserRepository,
                           UserSessionRepository userSessionRepository,
                           UserRegistrationOtpRepository userRegistrationOtpRepository,
                           BCryptPasswordEncoder passwordEncoder,
                           JavaMailSender javaMailSender,
                           AuthRegistrationOtpProperties otpProperties,
                           Environment environment) {
        this.appUserRepository = appUserRepository;
        this.userSessionRepository = userSessionRepository;
        this.userRegistrationOtpRepository = userRegistrationOtpRepository;
        this.passwordEncoder = passwordEncoder;
        this.javaMailSender = javaMailSender;
        this.otpProperties = otpProperties;
        this.environment = environment;
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
     * Generates OTP, stores pending registration, and sends OTP email to admin.
     */
    @Override
    @Transactional
    public AuthRegisterOtpResponseDTO requestRegistrationOtp(AuthRegisterRequestDTO request) {
        validateOtpConfiguration();

        String normalizedUsername = normalizeUsername(request.getUsername());
        if (appUserRepository.findByUsernameIgnoreCase(normalizedUsername).isPresent()) {
            throw new DuplicateResourceException("Username is already registered");
        }

        userRegistrationOtpRepository.deleteActivePendingByUsername(normalizedUsername);

        String rawOtp = generateOtp();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(otpProperties.getTtlMinutes());

        UserRegistrationOtp otpRequest = new UserRegistrationOtp();
        otpRequest.setUsername(normalizedUsername);
        otpRequest.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        otpRequest.setOtpHash(TokenUtils.sha256Hex(rawOtp));
        otpRequest.setOwnerEmail(ADMIN_REGISTRATION_EMAIL);
        otpRequest.setExpiresAt(expiry);
        otpRequest.setAttemptsLeft(otpProperties.getMaxAttempts());
        otpRequest.setVerified(false);
        userRegistrationOtpRepository.save(otpRequest);

        try {
            sendRegistrationOtpEmail(rawOtp, otpRequest);
        } catch (MailException ex) {
            userRegistrationOtpRepository.deleteById(otpRequest.getOtpId());
            log.error("Failed to send registration OTP email for username={}", normalizedUsername, ex);
            throw new IllegalStateException("Unable to send OTP email. Please try again.");
        }

        if (otpProperties.isDevLogEnabled() && environment.matchesProfiles("dev")) {
            log.info("DEV OTP for username={} is {}", normalizedUsername, rawOtp);
        }

        AuthRegisterOtpResponseDTO response = new AuthRegisterOtpResponseDTO();
        response.setUsername(normalizedUsername);
        response.setExpiresAt(expiry);
        response.setDeliveryEmailMasked(maskEmail(otpRequest.getOwnerEmail()));
        return response;
    }

    /**
     * Validates OTP and creates final user credential when OTP is valid.
     */
    @Override
    @Transactional
    public void verifyRegistrationOtp(AuthRegisterOtpVerifyRequestDTO request) {
        String normalizedUsername = normalizeUsername(request.getUsername());
        String rawOtp = request.getOtp().trim();

        UserRegistrationOtp otpRequest = userRegistrationOtpRepository
                .findTopByUsernameIgnoreCaseAndIsVerifiedAndConsumedAtIsNullOrderByCreatedAtDesc(normalizedUsername, 0)
                .orElseThrow(() -> new UnauthorizedException("No active OTP request found. Please request OTP again."));

        LocalDateTime now = LocalDateTime.now();
        if (otpRequest.getExpiresAt().isBefore(now)) {
            otpRequest.setConsumedAt(now);
            userRegistrationOtpRepository.save(otpRequest);
            throw new UnauthorizedException("OTP expired. Please request OTP again.");
        }

        if (otpRequest.getAttemptsLeft() <= 0) {
            otpRequest.setConsumedAt(now);
            userRegistrationOtpRepository.save(otpRequest);
            throw new UnauthorizedException("OTP attempts exceeded. Please request OTP again.");
        }

        if (!TokenUtils.sha256Hex(rawOtp).equals(otpRequest.getOtpHash())) {
            int attemptsLeft = Math.max(0, otpRequest.getAttemptsLeft() - 1);
            otpRequest.setAttemptsLeft(attemptsLeft);
            if (attemptsLeft == 0) {
                otpRequest.setConsumedAt(now);
            }
            userRegistrationOtpRepository.save(otpRequest);
            if (attemptsLeft == 0) {
                throw new UnauthorizedException("OTP attempts exceeded. Please request OTP again.");
            }
            throw new UnauthorizedException("Invalid OTP. Attempts left: " + attemptsLeft);
        }

        if (appUserRepository.findByUsernameIgnoreCase(normalizedUsername).isPresent()) {
            otpRequest.setConsumedAt(now);
            userRegistrationOtpRepository.save(otpRequest);
            throw new DuplicateResourceException("Username is already registered");
        }

        AppUser user = new AppUser();
        user.setUsername(normalizedUsername);
        user.setPasswordHash(otpRequest.getPasswordHash());
        user.setActive(true);
        appUserRepository.save(user);

        otpRequest.setVerified(true);
        otpRequest.setConsumedAt(now);
        userRegistrationOtpRepository.save(otpRequest);
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

    /** Validates required OTP configuration and safety bounds. */
    private void validateOtpConfiguration() {
        if (!otpProperties.isEnabled()) {
            throw new IllegalStateException("New user registration is currently disabled");
        }
        if (otpProperties.getTtlMinutes() <= 0 || otpProperties.getTtlMinutes() > 60) {
            throw new IllegalStateException("OTP TTL must be between 1 and 60 minutes");
        }
        if (otpProperties.getMaxAttempts() <= 0 || otpProperties.getMaxAttempts() > 10) {
            throw new IllegalStateException("OTP max attempts must be between 1 and 10");
        }
    }

    /** Normalizes username and ensures non-blank value post trim. */
    private String normalizeUsername(String username) {
        String normalized = username == null ? "" : username.trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        return normalized;
    }

    /** Generates zero-padded numeric OTP string. */
    private String generateOtp() {
        int bound = (int) Math.pow(10, OTP_LENGTH);
        int value = OTP_RANDOM.nextInt(bound);
        return String.format("%0" + OTP_LENGTH + "d", value);
    }

    /** Sends registration OTP email to configured admin email. */
    private void sendRegistrationOtpEmail(String rawOtp, UserRegistrationOtp otpRequest) {
        SimpleMailMessage message = new SimpleMailMessage();

        String fromEmail = otpProperties.getFromEmail();
        if (fromEmail != null && !fromEmail.isBlank()) {
            message.setFrom(fromEmail.trim());
        }

        message.setTo(otpRequest.getOwnerEmail());
        message.setSubject("MTS User Registration OTP");
        message.setText(buildOtpEmailBody(rawOtp, otpRequest));
        javaMailSender.send(message);
    }

    /** Creates deterministic OTP email body text for admin approval flow. */
    private String buildOtpEmailBody(String rawOtp, UserRegistrationOtp otpRequest) {
        return "A new user registration request was received for username: " + otpRequest.getUsername() + "\n\n"
                + "OTP: " + rawOtp + "\n"
                + "Valid for: " + otpProperties.getTtlMinutes() + " minutes\n"
                + "Max attempts: " + otpProperties.getMaxAttempts() + "\n\n"
                + "If this request is not expected, do not share this OTP.";
    }

    /** Masks admin email for UI response safety. */
    private String maskEmail(String email) {
        int atIndex = email.indexOf("@");
        if (atIndex <= 1) {
            return "***";
        }
        String prefix = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        String maskedPrefix = prefix.charAt(0) + "***" + prefix.charAt(prefix.length() - 1);
        return maskedPrefix + domain;
    }
}
