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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Calls the xAI Grok API (OpenAI-compatible endpoint).
 * Primary AI provider — used first for both text chat and image/vision analysis.
 * Falls back to Groq + Gemini if this service is unavailable or returns an error.
 */
@Service
public class GrokService {

    private static final Logger log = LoggerFactory.getLogger(GrokService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final String GROK_URL = "https://api.x.ai/v1/chat/completions";

    /** Text models tried in order — fast first, then more capable */
    private static final String[] TEXT_MODELS = {
            "grok-3-fast",
            "grok-3",
            "grok-2-1212",
    };

    /** Vision models for image analysis */
    private static final String[] VISION_MODELS = {
            "grok-2-vision-1212",
    };

    @Value("${xai.api-key:}")
    private String apiKey;

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    /**
     * Text chat with optional conversation history.
     *
     * @param systemPrompt  system instruction for the assistant
     * @param userMessage   latest user message
     * @param history       prior turns [{role: user|assistant, content: ...}], oldest first
     * @return reply text, or null if all models fail (caller should fall back)
     */
    public String chat(String systemPrompt, String userMessage, List<Map<String, String>> history) {
        if (!isConfigured()) return null;

        for (String model : TEXT_MODELS) {
            try {
                List<Map<String, Object>> messages = new ArrayList<>();
                messages.add(Map.of("role", "system", "content", systemPrompt));
                if (history != null) {
                    for (Map<String, String> h : history) {
                        messages.add(Map.of("role", h.get("role"), "content", h.get("content")));
                    }
                }
                messages.add(Map.of("role", "user", "content", userMessage));

                Map<String, Object> body = new LinkedHashMap<>();
                body.put("model", model);
                body.put("messages", messages);
                body.put("temperature", 0.7);
                body.put("max_tokens", 800);

                String reply = callGrok(body, model);
                if (reply != null) return reply;

            } catch (Exception e) {
                log.warn("Grok text model {} threw {}: {}", model, e.getClass().getSimpleName(), e.getMessage());
            }
        }
        log.error("All Grok text models failed");
        return null;
    }

    /**
     * Analyzes a base64-encoded image with a text prompt.
     *
     * @param prompt       what to analyse / question to answer about the image
     * @param base64Image  base64-encoded image bytes (no data-URI prefix)
     * @param mimeType     e.g. "image/jpeg" — null defaults to "image/jpeg"
     * @return reply text, or null if all vision models fail
     */
    public String analyzeImage(String prompt, String base64Image, String mimeType) {
        if (!isConfigured()) return null;
        String mime = mimeType == null ? "image/jpeg" : mimeType;

        for (String model : VISION_MODELS) {
            try {
                List<Object> contentParts = List.of(
                        Map.of("type", "text", "text", prompt),
                        Map.of("type", "image_url", "image_url",
                                Map.of("url", "data:" + mime + ";base64," + base64Image))
                );

                List<Map<String, Object>> messages = List.of(
                        Map.of("role", "user", "content", contentParts)
                );

                Map<String, Object> body = new LinkedHashMap<>();
                body.put("model", model);
                body.put("messages", messages);
                body.put("temperature", 0.4);
                body.put("max_tokens", 1200);

                String reply = callGrok(body, model);
                if (reply != null) return reply;

            } catch (Exception e) {
                log.warn("Grok vision model {} threw {}: {}", model, e.getClass().getSimpleName(), e.getMessage());
            }
        }
        log.error("All Grok vision models failed");
        return null;
    }

    // ── Internal ────────────────────────────────────────────────────────────────

    private String callGrok(Map<String, Object> body, String model) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(GROK_URL))
                .timeout(Duration.ofSeconds(45))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(body)))
                .build();

        HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        int code = res.statusCode();
        if (code < 200 || code >= 300) {
            log.warn("Grok model {} returned HTTP {} — body: {}", model, code, snippet(res.body()));
            return null;
        }
        JsonNode root = MAPPER.readTree(res.body());
        JsonNode content = root.path("choices").path(0).path("message").path("content");
        if (content.isMissingNode() || content.isNull()) {
            log.warn("Grok model {} returned 200 but no content. Body: {}", model, snippet(res.body()));
            return null;
        }
        String reply = content.asText();
        if (reply == null || reply.isBlank()) return null;
        log.info("Grok model {} succeeded ({} chars)", model, reply.length());
        return reply.trim();
    }

    private static String snippet(String s) {
        if (s == null) return "(empty)";
        return s.length() > 400 ? s.substring(0, 400) + "..." : s;
    }
}
