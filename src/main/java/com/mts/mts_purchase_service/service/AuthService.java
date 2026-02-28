package com.mts.mts_purchase_service.service;

import com.mts.mts_purchase_service.models.AuthLoginRequestDTO;
import com.mts.mts_purchase_service.models.AuthRegisterOtpResponseDTO;
import com.mts.mts_purchase_service.models.AuthRegisterOtpVerifyRequestDTO;
import com.mts.mts_purchase_service.models.AuthRegisterRequestDTO;
import com.mts.mts_purchase_service.models.AuthSessionInfoDTO;
import com.mts.mts_purchase_service.models.AuthSessionResponseDTO;
import com.mts.mts_purchase_service.models.AuthSetupRequestDTO;

/**
 * Auth service contract for login/session lifecycle.
 */
public interface AuthService {

    /** Sets up initial credentials when none exist. */
    void setupInitialCredential(AuthSetupRequestDTO request);

    /** Initiates OTP flow for creating a new user account. */
    AuthRegisterOtpResponseDTO requestRegistrationOtp(AuthRegisterRequestDTO request);

    /** Verifies OTP and creates user account if validation succeeds. */
    void verifyRegistrationOtp(AuthRegisterOtpVerifyRequestDTO request);

    /** Validates credentials and creates a session token. */
    AuthSessionResponseDTO login(AuthLoginRequestDTO request);

    /** Validates token and refreshes session expiry. */
    AuthSessionInfoDTO validateAndRefresh(String rawToken);

    /** Revokes session token. */
    void logout(String rawToken);
}
