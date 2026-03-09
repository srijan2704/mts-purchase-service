package com.mts.mts_purchase_service.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enforces per-IP rate limits for login and OTP auth flows.
 */
@Component
@Order(25)
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AuthRateLimitFilter.class);

    private static final String LOGIN_BUCKET = "LOGIN";
    private static final String OTP_BUCKET = "OTP";

    private final AuthRateLimitProperties properties;
    // Key format: <bucket>|<clientIp>, value holds request timestamps in current sliding window.
    private final ConcurrentHashMap<String, Deque<Long>> requestWindows = new ConcurrentHashMap<>();

    /** Creates filter with externalized rate-limit configuration. */
    public AuthRateLimitFilter(AuthRateLimitProperties properties) {
        this.properties = properties;
    }

    /**
     * Applies this filter only to POST auth endpoints that need throttling.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!properties.isEnabled()) {
            return true;
        }

        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();
        return !isRateLimitedAuthPath(path);
    }

    /**
     * Enforces per-IP sliding-window checks and returns HTTP 429 when threshold is exceeded.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        String bucket = resolveBucket(path);
        String clientIp = resolveClientIp(request);
        String key = bucket + "|" + clientIp;

        if (!allowRequest(key)) {
            long retryAfterSeconds = Math.max(1, properties.getWindowSeconds());
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
            response.setContentType("application/json");

            // Keep same response contract shape used by API error responses.
            String message = "Too many " + bucket.toLowerCase() + " requests. Please retry after "
                    + retryAfterSeconds + " seconds.";
            response.getWriter().write(buildRateLimitErrorJson(message));

            log.warn("AUTH_RATE_LIMIT_BLOCK bucket={} path={} clientIp={}", bucket, path, clientIp);
            return;
        }

        filterChain.doFilter(request, response);
    }

    /** Returns true only for throttled auth routes. */
    private boolean isRateLimitedAuthPath(String path) {
        return "/api/auth/login".equals(path)
                || "/api/auth/register/request-otp".equals(path)
                || "/api/auth/register/verify-otp".equals(path);
    }

    /** Maps endpoint to bucket so login and OTP limits are isolated. */
    private String resolveBucket(String path) {
        if ("/api/auth/login".equals(path)) {
            return LOGIN_BUCKET;
        }
        return OTP_BUCKET;
    }

    /** Resolves caller IP, preferring proxy-forwarded address when present. */
    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Sliding-window evaluator using in-memory timestamp queues.
     */
    private boolean allowRequest(String key) {
        long now = System.currentTimeMillis();
        long windowMillis = Math.max(1, properties.getWindowSeconds()) * 1000L;
        int maxRequests = Math.max(1, properties.getMaxRequests());

        Deque<Long> window = requestWindows.computeIfAbsent(key, ignored -> new ArrayDeque<>());
        // Synchronize per-key queue to keep add/evict/count atomic.
        synchronized (window) {
            // Evict entries older than configured window.
            while (!window.isEmpty() && window.peekFirst() <= now - windowMillis) {
                window.removeFirst();
            }

            if (window.size() >= maxRequests) {
                return false;
            }

            window.addLast(now);
            return true;
        }
    }

    /** Builds a small escaped JSON error payload without extra serialization dependencies. */
    private String buildRateLimitErrorJson(String message) {
        String escapedMessage = message.replace("\\", "\\\\").replace("\"", "\\\"");
        return "{\"success\":false,\"message\":\"" + escapedMessage + "\",\"data\":null}";
    }
}
