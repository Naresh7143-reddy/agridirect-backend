package com.agridirect.delivery;

import com.agridirect.delivery.dto.DeliveryDistanceMatrixDTO;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.LatLng;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Service for Google Maps API integration
 */
@Service
public class MapsService {
    
    private static final Logger logger = LoggerFactory.getLogger(MapsService.class);
    
    @Autowired
    private GeoApiContext geoApiContext;
    
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
            LatLng origin = new LatLng(sourceLatitude, sourceLongitude);
            LatLng destination = new LatLng(destLatitude, destLongitude);
            
            DistanceMatrix result = DistanceMatrixApi.newRequest(geoApiContext)
                    .origins(origin)
                    .destinations(destination)
                    .await();
            
            if (result.rows != null && result.rows.length > 0 && 
                result.rows[0].elements != null && result.rows[0].elements.length > 0) {
                
                com.google.maps.model.DistanceMatrixElement element = result.rows[0].elements[0];
                
                if (element.status != null && "OK".equals(element.status.toString())) {
                    DeliveryDistanceMatrixDTO dto = new DeliveryDistanceMatrixDTO();
                    dto.setDistanceMeters((double) element.distance.inMeters);
                    dto.setDurationSeconds((long) element.duration.inSeconds);
                    dto.setStatus("OK");
                    
                    logger.info("Distance: {} m, Duration: {} seconds", 
                               element.distance.inMeters, element.duration.inSeconds);
                    
                    return dto;
                } else {
                    String status = element.status != null ? element.status.toString() : "UNKNOWN";
                    DeliveryDistanceMatrixDTO dto = new DeliveryDistanceMatrixDTO();
                    dto.setStatus(status);
                    logger.warn("Distance Matrix API returned status: {}", status);
                    return dto;
                }
            }
            
            DeliveryDistanceMatrixDTO dto = new DeliveryDistanceMatrixDTO();
            dto.setStatus("ZERO_RESULTS");
            logger.warn("No results from Distance Matrix API");
            return dto;
            
        } catch (ApiException | InterruptedException | IOException e) {
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
