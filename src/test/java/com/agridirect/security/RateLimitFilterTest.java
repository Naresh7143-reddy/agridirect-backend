package com.agridirect.security;

import com.agridirect.auth.RateLimitFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RateLimitFilter — verifies the token-bucket rate limiter
 * correctly allows and blocks requests per IP.
 */
@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

    private RateLimitFilter filter;

    @Mock
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new RateLimitFilter();
    }

    // ── Non-rate-limited paths pass through ────────────────────────────────

    @Test
    @DisplayName("Non-auth path bypasses rate limiting — chain.doFilter called")
    void nonAuthPath_passesThrough() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/products");
        req.setRemoteAddr("10.0.0.1");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
        assertThat(res.getStatus()).isNotEqualTo(429);
    }

    // ── Rate-limited paths ─────────────────────────────────────────────────

    @Test
    @DisplayName("First 10 requests on /api/auth/login are allowed")
    void authLogin_first10Requests_allowed() throws Exception {
        for (int i = 0; i < 10; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/auth/login");
            req.setRemoteAddr("192.168.0.1");
            MockHttpServletResponse res = new MockHttpServletResponse();
            filter.doFilter(req, res, chain);
            assertThat(res.getStatus()).isNotEqualTo(429)
                    .as("Request " + (i + 1) + " should be allowed");
        }
        verify(chain, times(10)).doFilter(any(), any());
    }

    @Test
    @DisplayName("[SECURITY] 11th request on rate-limited path is blocked with 429")
    void authLogin_11thRequest_blocked429() throws Exception {
        // Exhaust the bucket
        for (int i = 0; i < 10; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/auth/login");
            req.setRemoteAddr("10.1.1.1");
            MockHttpServletResponse res = new MockHttpServletResponse();
            filter.doFilter(req, res, chain);
        }
        // 11th request should be blocked
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/auth/login");
        req.setRemoteAddr("10.1.1.1");
        MockHttpServletResponse res = new MockHttpServletResponse();
        filter.doFilter(req, res, chain);

        assertThat(res.getStatus()).isEqualTo(429);
        assertThat(res.getContentAsString()).contains("Too many requests");
    }

    @Test
    @DisplayName("[SECURITY] /api/auth/register is rate-limited")
    void authRegister_isRateLimited() throws Exception {
        for (int i = 0; i < 10; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/auth/register");
            req.setRemoteAddr("10.2.2.2");
            MockHttpServletResponse res = new MockHttpServletResponse();
            filter.doFilter(req, res, chain);
        }
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/auth/register");
        req.setRemoteAddr("10.2.2.2");
        MockHttpServletResponse res = new MockHttpServletResponse();
        filter.doFilter(req, res, chain);
        assertThat(res.getStatus()).isEqualTo(429);
    }

    @Test
    @DisplayName("Different IPs have separate rate-limit buckets")
    void differentIps_separateBuckets() throws Exception {
        // Exhaust IP A
        for (int i = 0; i < 11; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/auth/login");
            req.setRemoteAddr("10.3.0.1");
            MockHttpServletResponse res = new MockHttpServletResponse();
            filter.doFilter(req, res, chain);
        }
        // IP B should still be allowed
        MockHttpServletRequest reqB = new MockHttpServletRequest("POST", "/api/auth/login");
        reqB.setRemoteAddr("10.3.0.2");
        MockHttpServletResponse resB = new MockHttpServletResponse();
        filter.doFilter(reqB, resB, chain);
        assertThat(resB.getStatus()).isNotEqualTo(429);
    }

    @Test
    @DisplayName("[SECURITY] X-Forwarded-For header is used as the rate-limit key")
    void xForwardedFor_usedAsIpKey() throws Exception {
        // Exhaust with forwarded IP
        for (int i = 0; i < 10; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/auth/login");
            req.setRemoteAddr("10.4.0.1");
            req.addHeader("X-Forwarded-For", "203.0.113.1");
            MockHttpServletResponse res = new MockHttpServletResponse();
            filter.doFilter(req, res, chain);
        }
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/auth/login");
        req.setRemoteAddr("10.4.0.1");
        req.addHeader("X-Forwarded-For", "203.0.113.1");
        MockHttpServletResponse res = new MockHttpServletResponse();
        filter.doFilter(req, res, chain);
        assertThat(res.getStatus()).isEqualTo(429);
    }

    @Test
    @DisplayName("429 response body is valid JSON with message field")
    void blockedResponse_isJson() throws Exception {
        for (int i = 0; i < 11; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/auth/login");
            req.setRemoteAddr("10.5.0.1");
            MockHttpServletResponse res = new MockHttpServletResponse();
            filter.doFilter(req, res, chain);
        }
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/auth/login");
        req.setRemoteAddr("10.5.0.1");
        MockHttpServletResponse res = new MockHttpServletResponse();
        filter.doFilter(req, res, chain);

        assertThat(res.getContentType()).contains("application/json");
        assertThat(res.getContentAsString()).contains("message");
        assertThat(res.getContentAsString()).contains("success");
    }

    @Test
    @DisplayName("/api/auth/firebase path is rate-limited")
    void authFirebase_isRateLimited() throws Exception {
        for (int i = 0; i < 10; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/auth/firebase");
            req.setRemoteAddr("10.6.0.1");
            filter.doFilter(req, new MockHttpServletResponse(), chain);
        }
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/auth/firebase");
        req.setRemoteAddr("10.6.0.1");
        MockHttpServletResponse res = new MockHttpServletResponse();
        filter.doFilter(req, res, chain);
        assertThat(res.getStatus()).isEqualTo(429);
    }
}
