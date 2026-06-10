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

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${gemini.api-key:}")
    private String apiKey;

    @Value("${gemini.api-url:https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent}")
    private String apiUrl;

    // ─── Public entry points ──────────────────────────────────────────────────

    public String chat(String message, String language) {
        if (language == null || language.isBlank()) language = "English";
        String prompt =
                "You are Krishi AI, an expert farming assistant for Indian farmers. " +
                "Reply in " + language + " language. " +
                "Help with crop diseases, market prices, government schemes, fertilizers, " +
                "irrigation, weather, organic farming, and pest control. " +
                "If the question is not farming-related, politely redirect to farming topics. " +
                "Keep answers practical, simple, India-specific, and under 250 words. " +
                "Use emojis sparingly for clarity (e.g. 🌾 💰 📊). " +
                "Farmer's question: " + message;

        String reply = tryGemini(prompt);
        if (reply == null || reply.isBlank()) {
            log.warn("Gemini returned no reply for chat — using knowledge-base fallback");
            reply = FarmingKnowledge.findReply(message);
        }
        return reply;
    }

    public String detectDisease(String base64Image, String cropName, String mimeType) {
        String prompt = "You are an expert agricultural scientist helping Indian farmers. " +
                "Analyze this " + cropName + " crop image. " +
                "Identify any disease, pest, or nutrient deficiency. " +
                "Respond in this exact format:\n" +
                "ISSUE: <name>\n" +
                "SEVERITY: Mild|Moderate|Severe\n" +
                "CAUSE: <what causes this>\n" +
                "SYMPTOMS: <visible signs>\n" +
                "TREATMENT: <step by step>\n" +
                "PREVENTION: <future prevention>\n" +
                "URGENCY: Act immediately|Within a week|Monitor closely\n" +
                "Use simple language an Indian farmer can understand.";

        String body = "{"
                + "\"contents\":[{"
                + "\"parts\":["
                + "{\"inline_data\":{\"mime_type\":\"" + safe(mimeType, "image/jpeg") + "\",\"data\":\"" + base64Image + "\"}},"
                + "{\"text\":\"" + escapeJson(prompt) + "\"}"
                + "]"
                + "}]}";

        String reply = callGeminiRaw(body);
        if (reply == null || reply.isBlank()) {
            log.warn("Gemini disease detection failed for crop {} — using fallback", cropName);
            return "ISSUE: Unable to analyze image\n" +
                    "SEVERITY: Mild\n" +
                    "CAUSE: AI service temporarily unavailable\n" +
                    "SYMPTOMS: Could not process the image\n" +
                    "TREATMENT: Please consult your local agriculture extension officer (KVK)\n" +
                    "PREVENTION: Take a clearer photo in daylight and try again\n" +
                    "URGENCY: Monitor closely";
        }
        return reply;
    }

    public String getCropAdvice(String season, String location, String soilType, String waterAvailability) {
        String prompt = "You are an expert agricultural advisor for Indian farmers. " +
                "A farmer needs crop advice. " +
                "Location: " + safe(location, "India") + ". Season: " + safe(season, "current") +
                ". Soil type: " + safe(soilType, "loamy") +
                ". Water: " + safe(waterAvailability, "moderate") + ". " +
                "Suggest 3 best crops to grow right now. " +
                "For each crop provide:\n" +
                "CROP NAME, WHY SUITABLE, EXPECTED YIELD, MARKET PRICE, DEMAND, CARE TIPS, PROFIT ESTIMATE.\n" +
                "Be specific and practical for Indian farming.";

        String reply = tryGemini(prompt);
        if (reply == null || reply.isBlank()) {
            log.warn("Gemini crop advice failed — using fallback");
            return FarmingKnowledge.cropAdviceFallback(season, location, soilType, waterAvailability);
        }
        return reply;
    }

    public String getPriceForecast(String cropName, String location) {
        String prompt = "You are an agricultural market analyst for India. " +
                "Provide market analysis for " + safe(cropName, "crop") + " in " + safe(location, "India") + ".\n" +
                "Include: CURRENT PRICE RANGE, PRICE TREND (Rising/Falling/Stable), " +
                "NEXT 30 DAYS FORECAST, BEST TIME TO SELL, FACTORS, NEARBY MARKETS, TIPS.";

        String reply = tryGemini(prompt);
        if (reply == null || reply.isBlank()) {
            log.warn("Gemini price forecast failed — using fallback");
            return FarmingKnowledge.priceForecastFallback(cropName, location);
        }
        return reply;
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

    /** Actual HTTP call. Returns the response text or null on failure. */
    private String callGeminiRaw(String jsonBody) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("GEMINI_API_KEY not configured");
            return null;
        }
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl + "?key=" + apiKey))
                    .timeout(Duration.ofSeconds(25))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
            HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() < 200 || res.statusCode() >= 300) {
                log.error("Gemini API HTTP {} — body: {}", res.statusCode(),
                        res.body() != null ? res.body().substring(0, Math.min(500, res.body().length())) : "(empty)");
                return null;
            }
            JsonNode root = MAPPER.readTree(res.body());
            // candidates[0].content.parts[0].text
            JsonNode text = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
            if (text.isMissingNode() || text.isNull()) {
                log.error("Gemini response missing text field: {}", res.body().substring(0, Math.min(500, res.body().length())));
                return null;
            }
            String reply = text.asText();
            return reply == null || reply.isBlank() ? null : reply.trim();
        } catch (Exception e) {
            log.error("Gemini call exception: {}", e.getMessage());
            return null;
        }
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
