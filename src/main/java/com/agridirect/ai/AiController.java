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
    public ResponseEntity<ApiResponse<String>> detectDisease(
            @RequestParam MultipartFile image,
            @RequestParam String cropName) throws Exception {
        String base64 = Base64.getEncoder().encodeToString(image.getBytes());
        String mimeType = image.getContentType();
        String result = geminiService.detectDisease(base64, cropName, mimeType);
        return ResponseEntity.ok(ApiResponse.success("Disease analysis complete", result));
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

    @PostMapping("/chat")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<ApiResponse<String>> chat(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.success(
                geminiService.chat(body.get("message"), body.get("language"))));
    }
}
