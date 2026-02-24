package com.mts.mts_purchase_service.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Logs API request entry/exit for every user call at INFO level.
 */
@Component
@Order(20)
public class ApiRequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiRequestLoggingFilter.class);

    /**
     * Restricts this filter to application API endpoints only.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri == null || !uri.startsWith("/api/");
    }

    /**
     * Logs request entry and exit with method, path, status, user, and duration.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long startNanos = System.nanoTime();
        String method = request.getMethod();
        String path = request.getRequestURI();
        String query = request.getQueryString() == null ? "-" : request.getQueryString();
        String clientIp = resolveClientIp(request);

        log.info("REQ_IN method={} path={} query={} clientIp={}", method, path, query, clientIp);

        String errorType = null;
        int status = HttpServletResponse.SC_OK;
        try {
            filterChain.doFilter(request, response);
            status = response.getStatus();
        } catch (Exception ex) {
            status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            errorType = ex.getClass().getSimpleName();
            throw ex;
        } finally {
            long durationMs = (System.nanoTime() - startNanos) / 1_000_000;
            String user = String.valueOf(request.getAttribute("authUsername"));
            if (user == null || "null".equalsIgnoreCase(user)) {
                user = "anonymous";
            }

            if (errorType == null) {
                log.info("REQ_OUT method={} path={} status={} durationMs={} user={}",
                        method, path, response.getStatus(), durationMs, user);
            } else {
                log.info("REQ_OUT method={} path={} status={} durationMs={} user={} errorType={}",
                        method, path, status, durationMs, user, errorType);
            }
        }
    }

    /**
     * Resolves client IP, preferring proxy header when present.
     */
    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
