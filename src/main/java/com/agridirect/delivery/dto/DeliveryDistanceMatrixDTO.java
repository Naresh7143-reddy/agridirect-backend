package com.agridirect.delivery.dto;

/**
 * DTO to hold distance and time matrix data from Google Maps API
 */
public class DeliveryDistanceMatrixDTO {
    
    private Double distanceMeters;
    private Double distanceKm;
    private Long durationSeconds;
    private Integer durationMinutes;
    private String status; // OK, ZERO_RESULTS, INVALID_REQUEST, OVER_QUERY_LIMIT, REQUEST_DENIED, UNKNOWN_ERROR

    // Constructors
    public DeliveryDistanceMatrixDTO() {
    }

    public DeliveryDistanceMatrixDTO(Double distanceMeters, Long durationSeconds, String status) {
        this.distanceMeters = distanceMeters;
        this.distanceKm = distanceMeters / 1000.0;
        this.durationSeconds = durationSeconds;
        this.durationMinutes = (int) Math.ceil(durationSeconds / 60.0);
        this.status = status;
    }

    // Getters and Setters
    public Double getDistanceMeters() {
        return distanceMeters;
    }

    public void setDistanceMeters(Double distanceMeters) {
        this.distanceMeters = distanceMeters;
        if (distanceMeters != null) {
            this.distanceKm = distanceMeters / 1000.0;
        }
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Long durationSeconds) {
        this.durationSeconds = durationSeconds;
        if (durationSeconds != null) {
            this.durationMinutes = (int) Math.ceil(durationSeconds / 60.0);
        }
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isSuccessful() {
        return "OK".equals(status);
    }
}
