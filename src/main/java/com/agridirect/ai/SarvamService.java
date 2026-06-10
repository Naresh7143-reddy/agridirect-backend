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
 * Calls Sarvam AI (https://sarvam.ai) — India-focused LLM with native Indic
 * language support (Hindi, Telugu, Tamil, Kannada, etc.). Used as the primary
 * AI provider for the Krishi farming assistant when SARVAM_API_KEY is set.
 *
 * Endpoint is OpenAI-compatible (/v1/chat/completions). Models tried in order:
 * sarvam-30b (fast direct answers), sarvam-105b (heavier reasoning fallback).
 */
@Service
public class SarvamService {

    private static final Logger log = LoggerFactory.getLogger(SarvamService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final String SARVAM_URL = "https://api.sarvam.ai/v1/chat/completions";
    private static final String[] MODELS = {"sarvam-30b", "sarvam-105b"};

    @Value("${sarvam.api-key:}")
    private String apiKey;

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

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
                // sarvam-105b is a reasoning model — needs headroom for the
                // "thinking" tokens before the final answer is emitted in content.
                body.put("max_tokens", model.equals("sarvam-105b") ? 2500 : 800);

                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(SARVAM_URL))
                        .timeout(Duration.ofSeconds(60))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + apiKey)
                        .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(body)))
                        .build();

                HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
                int code = res.statusCode();
                if (code < 200 || code >= 300) {
                    log.warn("Sarvam {} returned HTTP {} — trying next. Body: {}",
                            model, code, snippet(res.body()));
                    continue;
                }
                JsonNode root = MAPPER.readTree(res.body());
                JsonNode content = root.path("choices").path(0).path("message").path("content");
                if (content.isMissingNode() || content.isNull() || content.asText().isBlank()) {
                    log.warn("Sarvam {} returned 200 but content empty (possibly truncated reasoning). Body: {}",
                            model, snippet(res.body()));
                    continue;
                }
                String reply = content.asText().trim();
                log.info("Sarvam {} succeeded ({} chars)", model, reply.length());
                return reply;
            } catch (Exception e) {
                log.warn("Sarvam {} threw {}: {}", model, e.getClass().getSimpleName(), e.getMessage());
            }
        }
        log.error("All Sarvam models failed");
        return null;
    }

    private static String snippet(String s) {
        if (s == null) return "(empty)";
        return s.length() > 400 ? s.substring(0, 400) + "..." : s;
    }
}
