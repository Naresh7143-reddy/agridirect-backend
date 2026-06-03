package com.agridirect.ai;

import com.agridirect.common.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class GeminiService {

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.api-url}")
    private String apiUrl;

    private String callGemini(String jsonBody) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl + "?key=" + apiKey))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Parse candidates[0].content.parts[0].text
            String body = response.body();
            int textIdx = body.indexOf("\"text\"");
            if (textIdx == -1) {
                throw new ApiException("Unexpected Gemini response: " + body, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            // Find the string value after "text":
            int start = body.indexOf("\"", textIdx + 7) + 1;
            int end = body.lastIndexOf("\"");
            // More robust: find the first quote after "text": and walk to closing quote
            String after = body.substring(textIdx + 7).stripLeading();
            // after starts with : "..."
            int colon = after.indexOf(':');
            String valueSection = after.substring(colon + 1).stripLeading();
            // valueSection starts with "..."
            int q1 = valueSection.indexOf('"') + 1;
            // find closing quote (unescaped)
            int q2 = q1;
            while (q2 < valueSection.length()) {
                char c = valueSection.charAt(q2);
                if (c == '\\') { q2 += 2; continue; }
                if (c == '"') break;
                q2++;
            }
            String raw = valueSection.substring(q1, q2);
            // Unescape JSON escape sequences
            return raw.replace("\\n", "\n").replace("\\t", "\t").replace("\\\"", "\"").replace("\\\\", "\\");

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Gemini API call failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public String detectDisease(String base64Image, String cropName, String mimeType) {
        String prompt = "You are an expert agricultural scientist helping Indian farmers. " +
                "Analyze this " + cropName + " crop image carefully. " +
                "Identify any disease, pest infestation, or nutrient deficiency. " +
                "Respond in this exact format:\n" +
                "ISSUE: [name of disease/pest/deficiency]\n" +
                "SEVERITY: [Mild/Moderate/Severe]\n" +
                "CAUSE: [what causes this]\n" +
                "SYMPTOMS: [what you can see]\n" +
                "TREATMENT: [step by step treatment]\n" +
                "PREVENTION: [how to prevent in future]\n" +
                "URGENCY: [Act immediately/Within a week/Monitor closely]\n" +
                "Use simple language an Indian farmer can understand.";

        String jsonBody = "{"
                + "\"contents\":[{"
                + "\"parts\":["
                + "{\"inline_data\":{\"mime_type\":\"" + mimeType + "\",\"data\":\"" + base64Image + "\"}},"
                + "{\"text\":\"" + escapeJson(prompt) + "\"}"
                + "]"
                + "}]}";

        return callGemini(jsonBody);
    }

    public String getCropAdvice(String season, String location, String soilType, String waterAvailability) {
        String prompt = "You are an expert agricultural advisor for Indian farmers. " +
                "A farmer needs crop advice. " +
                "Location: " + location + ". Season: " + season + ". Soil type: " + soilType +
                ". Water: " + waterAvailability + ". " +
                "Suggest exactly 3 best crops to grow right now. " +
                "For each crop provide:\n" +
                "CROP NAME:\n" +
                "WHY SUITABLE: [reason for this location/season/soil]\n" +
                "EXPECTED YIELD: [per acre]\n" +
                "MARKET PRICE: [current approximate price per kg in India]\n" +
                "DEMAND: [High/Medium/Low]\n" +
                "CARE TIPS: [2-3 important points]\n" +
                "PROFIT ESTIMATE: [approximate profit per acre]\n" +
                "Be specific and practical for Indian farming conditions.";

        return callGemini(buildTextRequest(prompt));
    }

    public String getPriceForecast(String cropName, String location) {
        String prompt = "You are an agricultural market analyst for India. " +
                "Provide market analysis for " + cropName + " in " + location + ".\n" +
                "CURRENT PRICE RANGE: [min-max per kg]\n" +
                "PRICE TREND: [Rising/Falling/Stable]\n" +
                "NEXT 30 DAYS FORECAST: [expected price movement]\n" +
                "BEST TIME TO SELL: [specific advice]\n" +
                "FACTORS AFFECTING PRICE: [key factors]\n" +
                "NEARBY MARKETS: [where to sell for best price near " + location + "]\n" +
                "TIPS: [2-3 practical tips to get best price]";

        return callGemini(buildTextRequest(prompt));
    }

    public String chat(String message, String language) {
        if (language == null || language.isBlank()) language = "English";
        String prompt = "You are AgriDirect AI assistant helping Indian farmers. " +
                "Always reply in " + language + " language. " +
                "You help with: crop diseases, farming techniques, market prices, government schemes, " +
                "weather effects on crops, organic farming, pest control, fertilizers, irrigation. " +
                "If question is not farming related, politely say you only help with farming topics. " +
                "Keep answers practical and simple. " +
                "Farmer question: " + message;

        return callGemini(buildTextRequest(prompt));
    }

    private String buildTextRequest(String prompt) {
        return "{\"contents\":[{\"parts\":[{\"text\":\"" + escapeJson(prompt) + "\"}]}]}";
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
