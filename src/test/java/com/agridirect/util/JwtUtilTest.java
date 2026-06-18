package com.agridirect.util;

import com.agridirect.common.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for JwtUtil — covers token generation, validation, claim
 * extraction, and security vulnerabilities (tampered / expired / wrong-secret tokens).
 */
class JwtUtilTest {

    private JwtUtil jwtUtil;

    private static final String SECRET =
            "test_jwt_secret_key_must_be_at_least_32_characters_long";
    private static final long EXPIRATION_MS = 3_600_000L; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", EXPIRATION_MS);
    }

    // ── Generation ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("generateToken produces a non-null JWT string")
    void generateToken_returnsNonNull() {
        String token = jwtUtil.generateToken("user-1", "FARMER", "+919876543210");
        assertThat(token).isNotNull().isNotBlank().contains(".");
    }

    @Test
    @DisplayName("generateRefreshToken produces a different token from access token")
    void generateRefreshToken_isDifferentFromAccess() {
        String access = jwtUtil.generateToken("user-1", "BUYER", "+919876543210");
        String refresh = jwtUtil.generateRefreshToken("user-1");
        assertThat(access).isNotEqualTo(refresh);
    }

    @Test
    @DisplayName("getAccessTokenExpirySeconds converts ms correctly")
    void expirySeconds_isCorrect() {
        assertThat(jwtUtil.getAccessTokenExpirySeconds()).isEqualTo(3600L);
    }

    // ── Claim extraction ───────────────────────────────────────────────────

    @Test
    @DisplayName("extractUserId returns the subject set during generation")
    void extractUserId_correct() {
        String token = jwtUtil.generateToken("user-42", "FARMER", "+919876543210");
        assertThat(jwtUtil.extractUserId(token)).isEqualTo("user-42");
    }

    @Test
    @DisplayName("extractRole returns the role claim")
    void extractRole_correct() {
        String token = jwtUtil.generateToken("user-1", "DELIVERY", "+919876543210");
        assertThat(jwtUtil.extractRole(token)).isEqualTo("DELIVERY");
    }

    @Test
    @DisplayName("extractPhone returns the phone claim")
    void extractPhone_correct() {
        String token = jwtUtil.generateToken("user-1", "BUYER", "+911234567890");
        assertThat(jwtUtil.extractPhone(token)).isEqualTo("+911234567890");
    }

    @Test
    @DisplayName("refresh token has 'type=refresh' claim")
    void refreshToken_hasTypeClaim() {
        String token = jwtUtil.generateRefreshToken("user-1");
        Claims claims = jwtUtil.extractClaims(token);
        assertThat(claims.get("type", String.class)).isEqualTo("refresh");
    }

    // ── Validation ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("isTokenValid returns true for a freshly generated token")
    void isTokenValid_validToken_returnsTrue() {
        String token = jwtUtil.generateToken("user-1", "FARMER", "+919876543210");
        assertThat(jwtUtil.isTokenValid(token)).isTrue();
    }

    @Test
    @DisplayName("[SECURITY] isTokenValid returns false for a blank string")
    void isTokenValid_blankString_returnsFalse() {
        assertThat(jwtUtil.isTokenValid("")).isFalse();
    }

    @Test
    @DisplayName("[SECURITY] isTokenValid returns false for random garbage")
    void isTokenValid_garbage_returnsFalse() {
        assertThat(jwtUtil.isTokenValid("not.a.jwt")).isFalse();
    }

    @Test
    @DisplayName("[SECURITY] Tampered token (modified payload) is rejected")
    void isTokenValid_tamperedToken_returnsFalse() {
        String token = jwtUtil.generateToken("user-1", "BUYER", "+919876543210");
        // Flip a character in the payload segment (index 1 of 3 dot-separated parts)
        String[] parts = token.split("\\.");
        String tamperedPayload = parts[1].substring(0, parts[1].length() - 1) + "X";
        String tampered = parts[0] + "." + tamperedPayload + "." + parts[2];
        assertThat(jwtUtil.isTokenValid(tampered)).isFalse();
    }

    @Test
    @DisplayName("[SECURITY] Token signed with different secret is rejected")
    void isTokenValid_wrongSecret_returnsFalse() {
        JwtUtil otherUtil = new JwtUtil();
        ReflectionTestUtils.setField(otherUtil, "secret",
                "completely_different_secret_key_min_32_chars_here");
        ReflectionTestUtils.setField(otherUtil, "expiration", EXPIRATION_MS);

        String foreignToken = otherUtil.generateToken("user-1", "ADMIN", "+919876543210");
        assertThat(jwtUtil.isTokenValid(foreignToken)).isFalse();
    }

    @Test
    @DisplayName("[SECURITY] Expired token is rejected by isTokenValid")
    void isTokenValid_expiredToken_returnsFalse() {
        JwtUtil shortLived = new JwtUtil();
        ReflectionTestUtils.setField(shortLived, "secret", SECRET);
        ReflectionTestUtils.setField(shortLived, "expiration", -1L); // already expired
        String expired = shortLived.generateToken("user-1", "BUYER", "+919876543210");
        assertThat(jwtUtil.isTokenValid(expired)).isFalse();
    }

    @Test
    @DisplayName("[SECURITY] extractClaims throws on expired token")
    void extractClaims_expiredToken_throws() {
        JwtUtil shortLived = new JwtUtil();
        ReflectionTestUtils.setField(shortLived, "secret", SECRET);
        ReflectionTestUtils.setField(shortLived, "expiration", -1L);
        String expired = shortLived.generateToken("user-1", "BUYER", "+919876543210");
        assertThatThrownBy(() -> jwtUtil.extractClaims(expired))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    @DisplayName("[SECURITY] extractClaims throws on malformed token")
    void extractClaims_malformed_throws() {
        assertThatThrownBy(() -> jwtUtil.extractClaims("abc.def"))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("[SECURITY] SQL injection string in userId does not break token — stored verbatim")
    void sqlInjection_inUserId_storedSafely() {
        String injection = "' OR '1'='1'; DROP TABLE users; --";
        String token = jwtUtil.generateToken(injection, "BUYER", "+919876543210");
        // Token should be valid but the injection string is just data, not executed
        assertThat(jwtUtil.isTokenValid(token)).isTrue();
        assertThat(jwtUtil.extractUserId(token)).isEqualTo(injection);
    }

    @Test
    @DisplayName("[SECURITY] XSS payload in role claim is stored verbatim, not interpreted")
    void xssPayload_inRole_storedVerbatim() {
        String xss = "<script>alert('xss')</script>";
        String token = jwtUtil.generateToken("user-1", xss, "+919876543210");
        assertThat(jwtUtil.extractRole(token)).isEqualTo(xss);
    }

    @Test
    @DisplayName("Two tokens for different users are different")
    void twoTokens_differentUsers_areDifferent() {
        String t1 = jwtUtil.generateToken("user-1", "BUYER", "+919876543210");
        String t2 = jwtUtil.generateToken("user-2", "BUYER", "+919876543210");
        assertThat(t1).isNotEqualTo(t2);
    }
}
