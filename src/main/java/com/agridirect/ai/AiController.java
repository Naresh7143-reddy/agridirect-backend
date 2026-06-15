package com.agridirect.ai;

import com.agridirect.common.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/api/farmer/ai")
public class AiController {

    @Autowired private GeminiService geminiService;

    @PostMapping("/disease")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<ApiResponse<DiseaseDetectionResult>> detectDisease(
            @RequestParam MultipartFile image,
            @RequestParam String cropName) throws Exception {
        String base64 = Base64.getEncoder().encodeToString(image.getBytes());
        String mimeType = image.getContentType();
        String rawResult = geminiService.detectDisease(base64, cropName, mimeType);
        DiseaseDetectionResult structured = DiseaseResultParser.parse(rawResult, cropName);
        return ResponseEntity.ok(ApiResponse.success("Disease analysis complete", structured));
    }

    @GetMapping("/crop-advice")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<ApiResponse<String>> getCropAdvice(
            @RequestParam String season,
            @RequestParam String location,
            @RequestParam String soilType,
            @RequestParam String waterAvailability) {
        return ResponseEntity.ok(ApiResponse.success(
                geminiService.getCropAdvice(season, location, soilType, waterAvailability)));
    }

    @GetMapping("/price-forecast")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<ApiResponse<String>> getPriceForecast(
            @RequestParam String cropName,
            @RequestParam String location) {
        return ResponseEntity.ok(ApiResponse.success(
                geminiService.getPriceForecast(cropName, location)));
    }

    @PostMapping("/advice")
    @PreAuthorize("hasAnyRole('FARMER','BUYER')")
    public ResponseEntity<ApiResponse<String>> postCropAdvice(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.success(
                geminiService.getCropAdvice(
                        body.get("season"),
                        body.get("location"),
                        body.get("soilType"),
                        body.get("waterAvailability"))));
    }

    @PostMapping("/price-forecast")
    @PreAuthorize("hasAnyRole('FARMER','BUYER')")
    public ResponseEntity<ApiResponse<String>> postPriceForecast(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.success(
                geminiService.getPriceForecast(body.get("cropName"), body.get("location"))));
    }

    @PostMapping("/chat")
    @PreAuthorize("hasAnyRole('FARMER','BUYER')")
    public ResponseEntity<ApiResponse<ChatResponse>> chat(@RequestBody Map<String, Object> body) {
        String message = body.get("message") == null ? null : body.get("message").toString();
        String language = body.get("language") == null ? "English" : body.get("language").toString();
        String reply = geminiService.chat(message, language);
        return ResponseEntity.ok(ApiResponse.success(new ChatResponse(reply, language)));
    }
}
