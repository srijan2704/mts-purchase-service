package com.mts.mts_purchase_service.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthRateLimitFilterTest {

    private AuthRateLimitFilter filter;

    /** Initializes filter with the same limits expected in production configuration. */
    @BeforeEach
    void setUp() {
        AuthRateLimitProperties properties = new AuthRateLimitProperties();
        properties.setEnabled(true);
        properties.setMaxRequests(5);
        properties.setWindowSeconds(30);
        filter = new AuthRateLimitFilter(properties);
    }

    /** Verifies that 6th login POST from same IP within 30s is rejected with HTTP 429. */
    @Test
    void loginRateLimit_shouldBlockSixthRequestWithinWindow() throws Exception {
        for (int i = 1; i <= 5; i++) {
            MockHttpServletResponse response = performPost("/api/auth/login", "1.1.1.1");
            assertEquals(200, response.getStatus(), "Request " + i + " should pass");
        }

        MockHttpServletResponse blockedResponse = performPost("/api/auth/login", "1.1.1.1");
        assertEquals(429, blockedResponse.getStatus());
        assertTrue(blockedResponse.getContentAsString().contains("Too many login requests"));
    }

    /** Verifies login and OTP have separate rate buckets for same client IP. */
    @Test
    void otpAndLogin_shouldUseDifferentBuckets() throws Exception {
        for (int i = 1; i <= 5; i++) {
            assertEquals(200, performPost("/api/auth/login", "2.2.2.2").getStatus());
        }

        // OTP bucket should still be available for same IP.
        MockHttpServletResponse otpResponse = performPost("/api/auth/register/request-otp", "2.2.2.2");
        assertEquals(200, otpResponse.getStatus());
    }

    /** Ensures non-auth routes are not throttled by auth filter. */
    @Test
    void nonAuthEndpoints_shouldNotBeRateLimitedByThisFilter() throws Exception {
        for (int i = 1; i <= 10; i++) {
            MockHttpServletResponse response = performPost("/api/sellers", "3.3.3.3");
            assertEquals(200, response.getStatus());
        }
    }

    /** Sends one mock POST through filter and returns captured response. */
    private MockHttpServletResponse performPost(String path, String clientIp) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
        request.setRemoteAddr(clientIp);

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, response, chain);
        return response;
    }
}
