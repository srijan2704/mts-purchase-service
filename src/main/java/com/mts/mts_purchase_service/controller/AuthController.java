package com.mts.mts_purchase_service.controller;

import com.mts.mts_purchase_service.models.ApiResponseDTO;
import com.mts.mts_purchase_service.models.AuthLoginRequestDTO;
import com.mts.mts_purchase_service.models.AuthSessionInfoDTO;
import com.mts.mts_purchase_service.models.AuthSessionResponseDTO;
import com.mts.mts_purchase_service.models.AuthSetupRequestDTO;
import com.mts.mts_purchase_service.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Auth controller for credential setup, login, and session lifecycle.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@Tag(name = "Auth", description = "Credential setup, login, logout, and session inspection APIs.")
public class AuthController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Creates initial credentials when none exist in DB.
     */
    @PostMapping("/setup")
    @Operation(
            summary = "Setup initial credential",
            description = "One-time bootstrap endpoint to create first login credential.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "SetupCredential",
                                    value = """
                                            {
                                              "username": "owner",
                                              "password": "MyStrongPassword123"
                                            }
                                            """
                            )
                    )
            )
    )
    public ResponseEntity<ApiResponseDTO<Void>> setup(@Valid @RequestBody AuthSetupRequestDTO request) {
        authService.setupInitialCredential(request);
        return ResponseEntity.ok(ApiResponseDTO.success("Credential setup completed successfully", null));
    }

    /**
     * Validates login credentials and returns bearer token.
     */
    @PostMapping("/login")
    @Operation(
            summary = "Login",
            description = "Returns session token with 15-minute expiry.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Login",
                                    value = """
                                            {
                                              "username": "owner",
                                              "password": "MyStrongPassword123"
                                            }
                                            """
                            )
                    )
            )
    )
    public ResponseEntity<ApiResponseDTO<AuthSessionResponseDTO>> login(@Valid @RequestBody AuthLoginRequestDTO request) {
        AuthSessionResponseDTO session = authService.login(request);
        return ResponseEntity.ok(ApiResponseDTO.success("Login successful", session));
    }

    /**
     * Revokes current token session.
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Revokes the current bearer token session.")
    public ResponseEntity<ApiResponseDTO<Void>> logout(@RequestHeader("Authorization") String authorizationHeader) {
        authService.logout(extractBearerToken(authorizationHeader));
        return ResponseEntity.ok(ApiResponseDTO.success("Logout successful", null));
    }

    /**
     * Returns current authenticated session info and refreshed expiry.
     */
    @GetMapping("/me")
    @Operation(summary = "Current session", description = "Returns current logged-in user and session expiry.")
    public ResponseEntity<ApiResponseDTO<AuthSessionInfoDTO>> me(@RequestHeader("Authorization") String authorizationHeader) {
        AuthSessionInfoDTO info = authService.validateAndRefresh(extractBearerToken(authorizationHeader));
        return ResponseEntity.ok(ApiResponseDTO.success("Session is valid", info));
    }

    /**
     * Extracts bearer token text from Authorization header.
     */
    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new com.mts.mts_purchase_service.exception.UnauthorizedException("Missing or invalid Authorization header");
        }
        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        if (token.isBlank()) {
            throw new com.mts.mts_purchase_service.exception.UnauthorizedException("Missing bearer token");
        }
        return token;
    }
}
