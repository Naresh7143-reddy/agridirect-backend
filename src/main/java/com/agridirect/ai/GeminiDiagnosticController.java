package com.agridirect.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Temporary public diagnostic endpoint to debug why Gemini calls are failing.
 * Exposes the actual HTTP status and response body Gemini returns, without
 * leaking the API key. Remove this controller once the issue is resolved.
 */
@RestController
@RequestMapping("/api/diagnostic")
public class GeminiDiagnosticController {

    @Value("${gemini.api-key:}")
    private String apiKey;

    @Value("${gemini.api-url:https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent}")
    private String apiUrl;

    @Value("${groq.api-key:}")
    private String groqKey;

    @org.springframework.web.bind.annotation.GetMapping("/groq")
    public Map<String, Object> testGroq() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("keyConfigured", groqKey != null && !groqKey.isBlank());
        result.put("keyLength", groqKey == null ? 0 : groqKey.length());
        result.put("keyPrefix", groqKey == null || groqKey.length() < 4 ? "" : groqKey.substring(0, 4) + "...");
        if (groqKey == null || groqKey.isBlank()) {
            result.put("status", "NOT_CONFIGURED");
            result.put("reason", "GROQ_API_KEY env var not set. Get free key at https://console.groq.com/keys and add to Render.");
            return result;
        }
        try {
            String body = "{\"model\":\"llama-3.3-70b-versatile\",\"messages\":[{\"role\":\"user\",\"content\":\"Say hello in one short sentence.\"}],\"max_tokens\":50}";
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                    .timeout(Duration.ofSeconds(25))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + groqKey)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            result.put("httpStatus", res.statusCode());
            String respBody = res.body();
            result.put("rawResponse", respBody == null ? null :
                    (respBody.length() > 1500 ? respBody.substring(0, 1500) + "..." : respBody));
            if (res.statusCode() >= 200 && res.statusCode() < 300) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(respBody);
                JsonNode content = root.path("choices").path(0).path("message").path("content");
                result.put("status", content.isMissingNode() ? "FAIL" : "OK");
                result.put("extractedReply", content.isMissingNode() ? null : content.asText());
            } else {
                result.put("status", "FAIL");
                result.put("reason", "Groq HTTP " + res.statusCode() + " — check key validity & model availability");
            }
        } catch (Exception e) {
            result.put("status", "FAIL");
            result.put("reason", "Exception: " + e.getClass().getSimpleName() + " — " + e.getMessage());
        }
        return result;
    }

    private static final String[] MODELS = {
            "gemini-2.0-flash", "gemini-2.0-flash-exp",
            "gemini-1.5-flash-latest", "gemini-1.5-flash-002",
            "gemini-1.5-pro-latest"
    };
    private static final String GEMINI_BASE = "https://generativelanguage.googleapis.com/v1beta/models/";

    @GetMapping("/gemini")
    public Map<String, Object> testGemini() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("keyConfigured", apiKey != null && !apiKey.isBlank());
        result.put("keyLength", apiKey == null ? 0 : apiKey.length());
        result.put("keyPrefix", apiKey == null || apiKey.length() < 4 ? "" : apiKey.substring(0, 4) + "...");

        if (apiKey == null || apiKey.isBlank()) {
            result.put("status", "FAIL");
            result.put("reason", "GEMINI_API_KEY environment variable is not set on Render");
            return result;
        }

        String body = "{\"contents\":[{\"parts\":[{\"text\":\"Say hello in one short sentence.\"}]}]}";
        Map<String, Object> attempts = new LinkedHashMap<>();
        result.put("attempts", attempts);

        for (String model : MODELS) {
            Map<String, Object> attempt = new LinkedHashMap<>();
            String url = GEMINI_BASE + model + ":generateContent";
            try {
                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(10))
                        .build();
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(url + "?key=" + apiKey))
                        .timeout(Duration.ofSeconds(25))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();
                HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
                attempt.put("httpStatus", res.statusCode());
                String respBody = res.body();
                String snippet = respBody == null ? null :
                        (respBody.length() > 800 ? respBody.substring(0, 800) + "..." : respBody);
                attempt.put("body", snippet);

                if (res.statusCode() >= 200 && res.statusCode() < 300) {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode root = mapper.readTree(respBody);
                    JsonNode text = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
                    if (!text.isMissingNode()) {
                        attempt.put("status", "OK");
                        attempt.put("extractedReply", text.asText());
                        attempts.put(model, attempt);
                        result.put("status", "OK");
                        result.put("workingModel", model);
                        result.put("extractedReply", text.asText());
                        return result;
                    }
                    attempt.put("status", "FAIL_NO_TEXT");
                } else {
                    attempt.put("status", "FAIL");
                    attempt.put("reason", interpretError(res.statusCode(), respBody));
                }
            } catch (Exception e) {
                attempt.put("status", "EXCEPTION");
                attempt.put("reason", e.getClass().getSimpleName() + " — " + e.getMessage());
            }
            attempts.put(model, attempt);
        }
        result.put("status", "FAIL");
        result.put("reason", "All models failed. See attempts for details.");
        return result;
    }

    private String interpretError(int code, String body) {
        String lower = body == null ? "" : body.toLowerCase();
        if (code == 400 && lower.contains("api key not valid")) return "API key is INVALID — generate a new one at https://aistudio.google.com/apikey and update GEMINI_API_KEY env var on Render";
        if (code == 403 && lower.contains("permission_denied")) return "API key lacks Generative Language API permission — enable 'Generative Language API' for the project in Google Cloud Console";
        if (code == 403 && lower.contains("billing")) return "Billing not enabled on the Google Cloud project — Gemini free tier still requires a billing account";
        if (code == 404) return "Model URL incorrect — check gemini.api-url config";
        if (code == 429) return "Rate limit hit — free tier allows 15 RPM, wait a minute or upgrade quota";
        if (code == 503) return "Gemini service overloaded — try again";
        return "HTTP " + code + " from Gemini API. See rawResponse for details.";
    }
}
