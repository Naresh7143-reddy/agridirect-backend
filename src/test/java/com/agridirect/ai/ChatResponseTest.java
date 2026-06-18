package com.agridirect.ai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for ChatResponse DTO.
 */
class ChatResponseTest {

    @Test
    @DisplayName("ChatResponse stores reply and language correctly")
    void chatResponse_storesFields() {
        ChatResponse r = new ChatResponse("Hello farmer!", "English");
        assertThat(r.getReply()).isEqualTo("Hello farmer!");
        assertThat(r.getLanguage()).isEqualTo("English");
    }

    @Test
    @DisplayName("ChatResponse handles null reply")
    void chatResponse_nullReply() {
        ChatResponse r = new ChatResponse(null, "Telugu");
        assertThat(r.getReply()).isNull();
        assertThat(r.getLanguage()).isEqualTo("Telugu");
    }

    @Test
    @DisplayName("ChatResponse handles null language")
    void chatResponse_nullLanguage() {
        ChatResponse r = new ChatResponse("reply text", null);
        assertThat(r.getReply()).isEqualTo("reply text");
        assertThat(r.getLanguage()).isNull();
    }
}
