package com.agridirect.delivery;

import com.agridirect.delivery.dto.DeliveryDistanceMatrixDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;

/**
 * Service for Google Maps API integration
 * Uses REST API instead of Java client library for better compatibility
 */
@Service
public class MapsService {
    
    private static final Logger logger = LoggerFactory.getLogger(MapsService.class);
    private static final String GOOGLE_MAPS_API_URL = "https://maps.googleapis.com/maps/api/distancematrix/json";
    
    @Value("${google-maps.api-key}")
    private String apiKey;
    
    /**
     * Get distance and duration between two coordinates using Distance Matrix API
     * 
     * @param sourceLatitude source latitude
     * @param sourceLongitude source longitude
     * @param destLatitude destination latitude
     * @param destLongitude destination longitude
     * @return DeliveryDistanceMatrixDTO containing distance and duration info
     */
    public DeliveryDistanceMatrixDTO getDistanceAndDuration(
            Double sourceLatitude, Double sourceLongitude,
            Double destLatitude, Double destLongitude) {
        
        try {
            String origins = sourceLatitude + "," + sourceLongitude;
            String destinations = destLatitude + "," + destLongitude;
            
            String url = GOOGLE_MAPS_API_URL + "?origins=" + URLEncoder.encode(origins, StandardCharsets.UTF_8) +
                    "&destinations=" + URLEncoder.encode(destinations, StandardCharsets.UTF_8) +
                    "&key=" + apiKey;
            
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
                String status = jsonResponse.get("status").getAsString();
                
                if ("OK".equals(status)) {
                    JsonArray rows = jsonResponse.getAsJsonArray("rows");
                    if (rows.size() > 0) {
                        JsonArray elements = rows.get(0).getAsJsonObject().getAsJsonArray("elements");
                        if (elements.size() > 0) {
                            JsonObject element = elements.get(0).getAsJsonObject();
                            String elementStatus = element.get("status").getAsString();
                            
                            if ("OK".equals(elementStatus)) {
                                DeliveryDistanceMatrixDTO dto = new DeliveryDistanceMatrixDTO();
                                
                                int distanceMeters = element.getAsJsonObject("distance").get("value").getAsInt();
                                long durationSeconds = element.getAsJsonObject("duration").get("value").getAsLong();
                                
                                dto.setDistanceMeters((double) distanceMeters);
                                dto.setDurationSeconds(durationSeconds);
                                dto.setStatus("OK");
                                
                                logger.info("Distance: {} m, Duration: {} seconds", distanceMeters, durationSeconds);
                                
                                return dto;
                            }
                        }
                    }
                }
                
                DeliveryDistanceMatrixDTO dto = new DeliveryDistanceMatrixDTO();
                dto.setStatus(status);
                logger.warn("Distance Matrix API returned status: {}", status);
                return dto;
            }
            
            DeliveryDistanceMatrixDTO dto = new DeliveryDistanceMatrixDTO();
            dto.setStatus("HTTP_ERROR_" + response.statusCode());
            logger.warn("HTTP Error from Google Maps API: {}", response.statusCode());
            return dto;
            
        } catch (IOException | InterruptedException e) {
            logger.error("Error calling Google Maps Distance Matrix API", e);
            DeliveryDistanceMatrixDTO dto = new DeliveryDistanceMatrixDTO();
            dto.setStatus("ERROR");
            return dto;
        }
    }
    
    /**
     * Calculate straight-line distance using Haversine formula (fallback if Maps API fails)
     * Returns distance in kilometers
     */
    public Double getHaversineDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        final int EARTH_RADIUS_KM = 6371;
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
    
    /**
     * Estimate delivery time based on distance (fallback if Maps API fails)
     * Assumes average speed of 20 km/h in urban areas
     * Returns time in minutes
     */
    public Integer estimateDeliveryTimeMinutes(Double distanceKm) {
        // Average urban delivery speed: 20 km/h
        final Double AVERAGE_SPEED_KMH = 20.0;
        
        // Add 5 minutes buffer for pickup/handover
        Double timeInHours = distanceKm / AVERAGE_SPEED_KMH;
        Integer timeInMinutes = (int) Math.ceil(timeInHours * 60) + 5;
        
        return timeInMinutes;
    }
}
