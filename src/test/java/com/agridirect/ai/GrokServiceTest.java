package com.agridirect.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for GrokService — verifies configuration guards and that
 * the service returns null (triggering fallback) when not configured.
 */
class GrokServiceTest {

    private GrokService grokService;

    @BeforeEach
    void setUp() {
        grokService = new GrokService();
    }

    // ── isConfigured ────────────────────────────────────────────────────────

    @Test
    @DisplayName("isConfigured returns false when api key is null")
    void isConfigured_nullKey_false() {
        ReflectionTestUtils.setField(grokService, "apiKey", null);
        assertThat(grokService.isConfigured()).isFalse();
    }

    @Test
    @DisplayName("isConfigured returns false when api key is blank")
    void isConfigured_blankKey_false() {
        ReflectionTestUtils.setField(grokService, "apiKey", "");
        assertThat(grokService.isConfigured()).isFalse();
    }

    @Test
    @DisplayName("isConfigured returns false when api key is whitespace only")
    void isConfigured_whitespaceKey_false() {
        ReflectionTestUtils.setField(grokService, "apiKey", "   ");
        assertThat(grokService.isConfigured()).isFalse();
    }

    @Test
    @DisplayName("isConfigured returns true when api key is set")
    void isConfigured_validKey_true() {
        ReflectionTestUtils.setField(grokService, "apiKey", "xai-testkey12345");
        assertThat(grokService.isConfigured()).isTrue();
    }

    // ── chat() when not configured ────────────────────────────────────────

    @Test
    @DisplayName("chat returns null when not configured (triggers fallback)")
    void chat_notConfigured_returnsNull() {
        ReflectionTestUtils.setField(grokService, "apiKey", "");
        String result = grokService.chat("You are an expert", "What is rice?", null);
        assertThat(result).isNull();
    }

    // ── analyzeImage() when not configured ───────────────────────────────

    @Test
    @DisplayName("analyzeImage returns null when not configured")
    void analyzeImage_notConfigured_returnsNull() {
        ReflectionTestUtils.setField(grokService, "apiKey", null);
        String result = grokService.analyzeImage("Identify disease", "base64data==", "image/jpeg");
        assertThat(result).isNull();
    }
}
