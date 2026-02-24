package com.mts.mts_purchase_service.config;

import com.mts.mts_purchase_service.exception.UnauthorizedException;
import com.mts.mts_purchase_service.models.AuthSessionInfoDTO;
import com.mts.mts_purchase_service.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Intercepts API requests and validates bearer token sessions.
 */
@Component
public class AuthTokenInterceptor implements HandlerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthService authService;
    private final Environment environment;

    public AuthTokenInterceptor(AuthService authService, Environment environment) {
        this.authService = authService;
        this.environment = environment;
    }

    /**
     * Validates session token and refreshes expiry before request reaches controller.
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");

        // Developer experience mode: allow unauthenticated API calls in dev profile.
        // If token is provided in dev, validate it and expose auth context attributes.
        boolean devProfileActive = environment.matchesProfiles("dev");
        if (devProfileActive && (authHeader == null || !authHeader.startsWith(BEARER_PREFIX))) {
            return true;
        }

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(BEARER_PREFIX.length()).trim();
        if (token.isBlank()) {
            throw new UnauthorizedException("Missing bearer token");
        }

        AuthSessionInfoDTO sessionInfo = authService.validateAndRefresh(token);
        request.setAttribute("authUsername", sessionInfo.getUsername());
        request.setAttribute("authExpiresAt", sessionInfo.getExpiresAt());
        return true;
    }
}
