package com.agridirect.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Calls the Groq API (https://groq.com) — free, no billing required, fast
 * Llama 3.3 70B inference. Used as the primary AI provider when
 * GROQ_API_KEY is set; falls back to Gemini if it's not.
 */
@Service
public class GroqService {

    private static final Logger log = LoggerFactory.getLogger(GroqService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";

    /** Models tried in order. All are free on Groq as of 2026-06. */
    private static final String[] MODELS = {
            "llama-3.3-70b-versatile",
            "llama-3.1-8b-instant",
            "llama3-70b-8192",
            "mixtral-8x7b-32768",
    };

    @Value("${groq.api-key:}")
    private String apiKey;

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    /**
     * Sends a chat completion request. Returns the assistant reply text on
     * success, or null on failure (caller should fall back to Gemini or
     * FarmingKnowledge).
     */
    public String chat(String systemPrompt, String userMessage) {
        if (!isConfigured()) return null;

        for (String model : MODELS) {
            try {
                Map<String, Object> body = new LinkedHashMap<>();
                body.put("model", model);
                body.put("messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userMessage)
                ));
                body.put("temperature", 0.7);
                body.put("max_tokens", 800);

                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(GROQ_URL))
                        .timeout(Duration.ofSeconds(25))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + apiKey)
                        .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(body)))
                        .build();

                HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
                int code = res.statusCode();
                if (code < 200 || code >= 300) {
                    log.warn("Groq model {} returned HTTP {} — trying next. Body: {}",
                            model, code, snippet(res.body()));
                    continue;
                }
                JsonNode root = MAPPER.readTree(res.body());
                JsonNode content = root.path("choices").path(0).path("message").path("content");
                if (content.isMissingNode() || content.isNull()) {
                    log.warn("Groq model {} returned 200 but no content. Body: {}", model, snippet(res.body()));
                    continue;
                }
                String reply = content.asText();
                if (reply != null && !reply.isBlank()) {
                    log.info("Groq model {} succeeded ({} chars)", model, reply.length());
                    return reply.trim();
                }
            } catch (Exception e) {
                log.warn("Groq model {} threw {}: {}", model, e.getClass().getSimpleName(), e.getMessage());
            }
        }
        log.error("All Groq models failed");
        return null;
    }

    private static String snippet(String s) {
        if (s == null) return "(empty)";
        return s.length() > 400 ? s.substring(0, 400) + "..." : s;
    }
}
