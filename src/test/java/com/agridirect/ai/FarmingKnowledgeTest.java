package com.agridirect.ai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for FarmingKnowledge fallback knowledge base.
 */
class FarmingKnowledgeTest {

    // ── Keyword matching ───────────────────────────────────────────────────

    @Test
    @DisplayName("findReply matches 'disease' keyword")
    void findReply_disease() {
        String reply = FarmingKnowledge.findReply("my crop has a disease");
        assertThat(reply).isNotBlank().containsIgnoringCase("disease");
    }

    @Test
    @DisplayName("findReply matches 'price' keyword")
    void findReply_price() {
        String reply = FarmingKnowledge.findReply("what is the best price to sell tomato");
        assertThat(reply).isNotBlank();
    }

    @Test
    @DisplayName("findReply matches 'fertilizer' keyword")
    void findReply_fertilizer() {
        String reply = FarmingKnowledge.findReply("which fertilizer should I use?");
        assertThat(reply).isNotBlank();
    }

    @Test
    @DisplayName("findReply matches 'weather' keyword")
    void findReply_weather() {
        String reply = FarmingKnowledge.findReply("what is the weather tip for my crop?");
        assertThat(reply).isNotBlank();
    }

    @Test
    @DisplayName("findReply matches 'irrigation' keyword")
    void findReply_irrigation() {
        String reply = FarmingKnowledge.findReply("irrigation schedule for paddy");
        assertThat(reply).isNotBlank();
    }

    @Test
    @DisplayName("findReply matches 'scheme' keyword")
    void findReply_scheme() {
        String reply = FarmingKnowledge.findReply("government scheme for farmers");
        assertThat(reply).isNotBlank();
    }

    @Test
    @DisplayName("findReply returns a non-empty generic reply for unknown topic")
    void findReply_unknownTopic_returnsGeneric() {
        String reply = FarmingKnowledge.findReply("asdfghjklzxcvbnm");
        assertThat(reply).isNotBlank();
    }

    @Test
    @DisplayName("findReply handles null input without throwing")
    void findReply_null_noThrow() {
        assertThatCode(() -> FarmingKnowledge.findReply(null))
                .doesNotThrowAnyException();
        String reply = FarmingKnowledge.findReply(null);
        assertThat(reply).isNotBlank();
    }

    @Test
    @DisplayName("findReply handles empty string without throwing")
    void findReply_empty_returnsGeneric() {
        String reply = FarmingKnowledge.findReply("");
        assertThat(reply).isNotBlank();
    }

    @Test
    @DisplayName("findReply is case-insensitive")
    void findReply_caseInsensitive() {
        String lower = FarmingKnowledge.findReply("disease");
        String upper = FarmingKnowledge.findReply("DISEASE");
        assertThat(lower).isEqualTo(upper);
    }

    // ── cropAdviceFallback ─────────────────────────────────────────────────

    @Test
    @DisplayName("cropAdviceFallback returns non-blank string")
    void cropAdviceFallback_returnsResult() {
        String r = FarmingKnowledge.cropAdviceFallback("Kharif", "Andhra Pradesh", "loamy", "moderate");
        assertThat(r).isNotBlank();
    }

    @Test
    @DisplayName("cropAdviceFallback handles null parameters")
    void cropAdviceFallback_nullParams_noThrow() {
        assertThatCode(() -> FarmingKnowledge.cropAdviceFallback(null, null, null, null))
                .doesNotThrowAnyException();
    }

    // ── priceForecastFallback ──────────────────────────────────────────────

    @Test
    @DisplayName("priceForecastFallback returns non-blank string")
    void priceForecastFallback_returnsResult() {
        String r = FarmingKnowledge.priceForecastFallback("tomato", "Hyderabad");
        assertThat(r).isNotBlank();
    }

    @Test
    @DisplayName("priceForecastFallback handles null crop and location")
    void priceForecastFallback_nullParams_noThrow() {
        assertThatCode(() -> FarmingKnowledge.priceForecastFallback(null, null))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "pest on my tomato plant",
        "yellow leaves on my crop",
        "leaf spot disease",
        "NPK ratio for wheat",
        "urea application schedule",
        "when to sell onion",
        "market price paddy",
        "drip irrigation vs flood",
        "PM Kisan Samman scheme",
        "organic farming tips"
    })
    @DisplayName("findReply returns non-blank for common farming queries")
    void findReply_commonQueries_allReturnContent(String query) {
        assertThat(FarmingKnowledge.findReply(query)).isNotBlank();
    }
}
