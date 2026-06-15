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

    @Autowired private GroqService groqService;

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

    // ─── Public entry points ──────────────────────────────────────────────────

    public String chat(String message, String language) {
        return chat(message, language, null);
    }

    /**
     * @param history prior conversation turns ({@code role}: "user"|"assistant", {@code content}: text),
     *                 oldest first, for multi-turn memory. May be null/empty.
     */
    public String chat(String message, String language, java.util.List<Map<String, String>> history) {
        if (language == null || language.isBlank()) language = "English";

        String systemPrompt =
                "You are Krishi AI, an expert farming assistant for Indian farmers. " +
                "Reply in " + language + " language. " +
                "Help with crop diseases, market prices, government schemes, fertilizers, " +
                "irrigation, weather, organic farming, and pest control. " +
                "If the question is not farming-related, politely redirect to farming topics. " +
                "Keep answers practical, simple, India-specific, and under 250 words. " +
                "Use emojis sparingly for clarity (e.g. 🌾 💰 📊). " +
                "Use the prior conversation for context (e.g. remember the crop, location, or " +
                "problem the farmer already mentioned) and avoid repeating questions already answered.";

        // 1. Try Groq (Llama 3.3 70B — free, fast)
        if (groqService.isConfigured()) {
            String groqReply = groqService.chat(systemPrompt, message, history);
            if (groqReply != null && !groqReply.isBlank()) return groqReply;
            log.warn("Groq failed — falling through to Gemini");
        }

        // 2. Try Gemini (requires billing / valid AIza key)
        StringBuilder prompt = new StringBuilder(systemPrompt);
        if (history != null && !history.isEmpty()) {
            prompt.append("\n\nConversation so far:");
            for (Map<String, String> turn : history) {
                prompt.append("\n").append("user".equals(turn.get("role")) ? "Farmer" : "Krishi AI")
                        .append(": ").append(turn.get("content"));
            }
        }
        prompt.append("\n\nFarmer's question: ").append(message);
        String reply = tryGemini(prompt.toString());
        if (reply != null && !reply.isBlank()) return reply;

        // 3. Last resort: keyword-matched knowledge base
        log.warn("Both Groq and Gemini failed for chat — using knowledge-base fallback");
        return FarmingKnowledge.findReply(message);
    }

    public String detectDisease(String base64Image, String cropName, String mimeType) {
        String visionPrompt = "You are an expert agricultural scientist helping Indian farmers. " +
                "Analyze this " + cropName + " crop image. " +
                "Identify any disease, pest, or nutrient deficiency. Respond in this exact format:\n" +
                "ISSUE: <name>\nSEVERITY: Mild|Moderate|Severe\nCAUSE: <cause>\n" +
                "SYMPTOMS: <visible signs>\nTREATMENT: <step by step>\n" +
                "PREVENTION: <future prevention>\nURGENCY: Act immediately|Within a week|Monitor closely";

        // 1. Try Groq Vision first (free, fast, no billing)
        if (groqService.isConfigured()) {
            String groqReply = groqService.analyzeImage(visionPrompt, base64Image, mimeType);
            if (groqReply != null && !groqReply.isBlank()) {
                log.info("Disease detection: Groq Vision succeeded");
                return groqReply;
            }
            log.warn("Groq Vision failed — falling through to Gemini Vision");
        }

        // 2. Fall back to Gemini Vision
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
        String systemPrompt = "You are an expert agricultural advisor for Indian farmers. " +
                "Suggest 3 best crops to grow right now. " +
                "For each crop provide: CROP NAME, WHY SUITABLE, EXPECTED YIELD, MARKET PRICE, DEMAND, CARE TIPS, PROFIT ESTIMATE.\n" +
                "Be specific and practical for Indian farming.";
        String userMsg = "Location: " + safe(location, "India") +
                ". Season: " + safe(season, "current") +
                ". Soil type: " + safe(soilType, "loamy") +
                ". Water: " + safe(waterAvailability, "moderate");

        if (groqService.isConfigured()) {
            String r = groqService.chat(systemPrompt, userMsg);
            if (r != null && !r.isBlank()) return r;
        }
        String reply = tryGemini(systemPrompt + "\n\n" + userMsg);
        if (reply != null && !reply.isBlank()) return reply;
        log.warn("All AI providers failed for crop advice — using fallback");
        return FarmingKnowledge.cropAdviceFallback(season, location, soilType, waterAvailability);
    }

    public String getPriceForecast(String cropName, String location) {
        String systemPrompt = "You are an agricultural market analyst for India. " +
                "Include: CURRENT PRICE RANGE, PRICE TREND (Rising/Falling/Stable), " +
                "NEXT 30 DAYS FORECAST, BEST TIME TO SELL, FACTORS, NEARBY MARKETS, TIPS.";
        String userMsg = "Provide market analysis for " + safe(cropName, "crop") + " in " + safe(location, "India") + ".";

        if (groqService.isConfigured()) {
            String r = groqService.chat(systemPrompt, userMsg);
            if (r != null && !r.isBlank()) return r;
        }
        String reply = tryGemini(systemPrompt + "\n\n" + userMsg);
        if (reply != null && !reply.isBlank()) return reply;
        log.warn("All AI providers failed for price forecast — using fallback");
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
