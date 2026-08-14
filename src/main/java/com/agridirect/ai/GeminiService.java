package com.agridirect.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * Calls the Google Gemini API for AI farming assistance, with a robust JSON
 * parser (Jackson) and a built-in fallback knowledge base so the assistant
 * never returns an empty/error message even if Gemini is unreachable,
 * rate-limited, or misconfigured.
 *
 * Errors are logged with detail but never bubble out — the worst case is the
 * user gets a relevant FarmingKnowledge fallback instead of a real LLM reply.
 */
@Service
public class GeminiService {

    @Autowired private GrokService grokService;   // primary (xAI Grok)
    @Autowired private GroqService groqService;   // fallback #1

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${gemini.api-key:}")
    private String apiKey;

    /**
     * Configurable base URL. We ignore the model segment of this URL and pick
     * from MODEL_FALLBACKS below — that way an outdated GEMINI_API_URL on
     * Render (e.g. pointing at the retired gemini-1.5-flash) still works.
     */
    @Value("${gemini.api-url:https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent}")
    private String apiUrl;

    /**
     * Models tried in order. As Google retires models, we fall through to the
     * next one automatically rather than hard-failing. All are currently
     * accessible on free-tier (as of 2026-06).
     */
    private static final String[] MODEL_FALLBACKS = {
            "gemini-2.0-flash",
            "gemini-2.0-flash-exp",
            "gemini-1.5-flash-latest",
            "gemini-1.5-flash-002",
            "gemini-1.5-pro-latest",
    };
    private static final String GEMINI_BASE = "https://generativelanguage.googleapis.com/v1beta/models/";

    // ─── Local AI Python Server Integration ───────────────────────────────────

    private String callLocalAi(String path, String jsonBody) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8000" + path))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
            HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() == 200) {
                JsonNode root = MAPPER.readTree(res.body());
                return root.path("response").asText();
            } else {
                log.warn("Local AI endpoint {} returned HTTP status {}", path, res.statusCode());
            }
        } catch (Exception e) {
            log.error("Failed to connect to local AI service at localhost:8000. Is it running? Error: {}", e.getMessage());
        }
        return null;
    }

    public String chat(String message, String language) {
        return chat(message, language, null);
    }

    public String chat(String message, String language, java.util.List<Map<String, String>> history) {
        if (language == null || language.isBlank()) language = "English";
        try {
            java.util.List<Map<String, String>> reqHistory = history != null ? history : java.util.Collections.emptyList();
            String body = MAPPER.writeValueAsString(Map.of(
                    "message", message,
                    "language", language,
                    "history", reqHistory
            ));
            String reply = callLocalAi("/api/ai/chat", body);
            if (reply != null && !reply.isBlank()) return reply;
        } catch (Exception e) {
            log.error("Error formatting local chat request: {}", e.getMessage());
        }
        log.warn("Local AI service failed for chat — using local knowledge base");
        return FarmingKnowledge.findReply(message);
    }

    public String detectDisease(String base64Image, String cropName, String mimeType) {
        try {
            String body = MAPPER.writeValueAsString(Map.of(
                    "base64Image", base64Image,
                    "cropName", cropName,
                    "mimeType", mimeType != null ? mimeType : "image/jpeg"
            ));
            String reply = callLocalAi("/api/ai/disease", body);
            if (reply != null && !reply.isBlank()) return reply;
        } catch (Exception e) {
            log.error("Error formatting local disease detection request: {}", e.getMessage());
        }
        return "ISSUE: Local AI Offline\n" +
                "SEVERITY: Mild\n" +
                "CAUSE: Local AI service is not running on localhost:8000\n" +
                "SYMPTOMS: Connection refused\n" +
                "TREATMENT: Start the local AI service using run_setup.bat\n" +
                "PREVENTION: Ensure Python service is active\n" +
                "URGENCY: Act immediately";
    }

    public String getCropAdvice(String season, String location, String soilType, String waterAvailability) {
        try {
            String body = MAPPER.writeValueAsString(Map.of(
                    "season", season,
                    "location", location,
                    "soilType", soilType,
                    "waterAvailability", waterAvailability
            ));
            String reply = callLocalAi("/api/ai/crop-advice", body);
            if (reply != null && !reply.isBlank()) return reply;
        } catch (Exception e) {
            log.error("Error formatting local crop advice request: {}", e.getMessage());
        }
        log.warn("Local AI service failed for crop advice — using local fallback");
        return FarmingKnowledge.cropAdviceFallback(season, location, soilType, waterAvailability);
    }

    public String getPriceForecast(String cropName, String location) {
        try {
            String body = MAPPER.writeValueAsString(Map.of(
                    "cropName", cropName,
                    "location", location
            ));
            String reply = callLocalAi("/api/ai/price-forecast", body);
            if (reply != null && !reply.isBlank()) return reply;
        } catch (Exception e) {
            log.error("Error formatting local price forecast request: {}", e.getMessage());
        }
        log.warn("Local AI service failed for price forecast — using local fallback");
        return FarmingKnowledge.priceForecastFallback(cropName, location);
    }

    // ─── Gemini HTTP call & JSON parsing ──────────────────────────────────────

    /** Builds a text-only request body and calls Gemini. Returns null on failure. */
    private String tryGemini(String prompt) {
        try {
            String body = MAPPER.writeValueAsString(Map.of(
                "contents", new Object[]{
                    Map.of("parts", new Object[]{ Map.of("text", prompt) })
                }
            ));
            return callGeminiRaw(body);
        } catch (Exception e) {
            log.error("Failed to build Gemini request: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Tries each model in MODEL_FALLBACKS until one returns 2xx with text.
     * Returns the parsed reply, or null if all attempts fail.
     */
    private String callGeminiRaw(String jsonBody) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("GEMINI_API_KEY not configured");
            return null;
        }
        for (String model : MODEL_FALLBACKS) {
            String url = GEMINI_BASE + model + ":generateContent";
            try {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(url + "?key=" + apiKey))
                        .timeout(Duration.ofSeconds(25))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();
                HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
                int code = res.statusCode();
                if (code < 200 || code >= 300) {
                    log.warn("Gemini model {} returned HTTP {} — trying next fallback. Body: {}",
                            model, code, snippet(res.body()));
                    continue;
                }
                JsonNode root = MAPPER.readTree(res.body());
                JsonNode text = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
                if (text.isMissingNode() || text.isNull()) {
                    log.warn("Gemini model {} returned 200 but no text. Body: {}", model, snippet(res.body()));
                    continue;
                }
                String reply = text.asText();
                if (reply != null && !reply.isBlank()) {
                    log.info("Gemini model {} succeeded ({} chars)", model, reply.length());
                    return reply.trim();
                }
            } catch (Exception e) {
                log.warn("Gemini model {} threw {}: {}", model, e.getClass().getSimpleName(), e.getMessage());
            }
        }
        log.error("All Gemini model fallbacks failed");
        return null;
    }

    private static String snippet(String s) {
        if (s == null) return "(empty)";
        return s.length() > 400 ? s.substring(0, 400) + "..." : s;
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private static String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static String safe(String v, String fallback) {
        return v == null || v.isBlank() ? fallback : v;
    }
}
