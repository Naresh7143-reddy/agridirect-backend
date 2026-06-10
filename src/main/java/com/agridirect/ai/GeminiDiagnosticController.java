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

    @GetMapping("/gemini")
    public Map<String, Object> testGemini() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("keyConfigured", apiKey != null && !apiKey.isBlank());
        result.put("keyLength", apiKey == null ? 0 : apiKey.length());
        result.put("keyPrefix", apiKey == null || apiKey.length() < 4 ? "" : apiKey.substring(0, 4) + "...");
        result.put("apiUrl", apiUrl);

        if (apiKey == null || apiKey.isBlank()) {
            result.put("status", "FAIL");
            result.put("reason", "GEMINI_API_KEY environment variable is not set on Render");
            return result;
        }

        try {
            String body = "{\"contents\":[{\"parts\":[{\"text\":\"Say hello in one short sentence.\"}]}]}";
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl + "?key=" + apiKey))
                    .timeout(Duration.ofSeconds(25))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

            result.put("httpStatus", res.statusCode());
            String respBody = res.body();
            // Cap response body at 2KB
            result.put("rawResponse", respBody == null ? null :
                    (respBody.length() > 2000 ? respBody.substring(0, 2000) + "..." : respBody));

            if (res.statusCode() >= 200 && res.statusCode() < 300) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(respBody);
                JsonNode text = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
                result.put("status", text.isMissingNode() ? "FAIL" : "OK");
                result.put("extractedReply", text.isMissingNode() ? null : text.asText());
                if (text.isMissingNode()) {
                    result.put("reason", "Response 200 but missing candidates[0].content.parts[0].text — possibly safety-blocked or quota issue");
                }
            } else {
                result.put("status", "FAIL");
                result.put("reason", interpretError(res.statusCode(), respBody));
            }
        } catch (Exception e) {
            result.put("status", "FAIL");
            result.put("reason", "Exception: " + e.getClass().getSimpleName() + " — " + e.getMessage());
        }
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
