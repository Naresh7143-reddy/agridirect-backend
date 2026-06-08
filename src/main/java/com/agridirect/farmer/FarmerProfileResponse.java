package com.agridirect.farmer;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO returned by /api/farmer/profile (and public farmer profile lookups).
 * Shaped to match the mobile app's `FarmerProfile` TypeScript interface —
 * the raw JPA entity stores `location` as a single string and is missing
 * several fields (rating, reviewCount, totalSales, totalProducts,
 * certifications, isVerified, isAvailable, bankDetails, photoUrl, bio,
 * updatedAt) that the app expects, which previously caused the app to crash
 * (e.g. calling `.toFixed()` on an undefined `rating`).
 */
public class FarmerProfileResponse {

    public static class LocationDto {
        public String address;
        public String city;
        public String state;
        public String pincode;
        public Double lat;
        public Double lng;

        public LocationDto(String address, String city, String state, String pincode) {
            this.address = address;
            this.city = city;
            this.state = state;
            this.pincode = pincode;
        }
    }

    private String id;
    private String userId;
    private String farmName;
    private String bio;
    private String photoUrl;
    private LocationDto location;
    private List<String> certifications;
    private boolean isVerified;
    private boolean isAvailable;
    private double rating;
    private int reviewCount;
    private long totalSales;
    private long totalProducts;
    private Object bankDetails;
    private String createdAt;
    private String updatedAt;

    public static FarmerProfileResponse from(FarmerProfile profile, long totalProducts, long totalSales) {
        FarmerProfileResponse dto = new FarmerProfileResponse();
        dto.id = profile.getId() != null ? profile.getId().toString() : null;
        dto.userId = profile.getUserId() != null ? profile.getUserId().toString() : null;
        dto.farmName = profile.getFarmName();
        dto.bio = null;
        dto.photoUrl = null;
        dto.location = parseLocation(profile.getLocation());
        dto.certifications = List.of();
        dto.isVerified = profile.isVerified();
        dto.isAvailable = true;
        dto.rating = 0.0;
        dto.reviewCount = 0;
        dto.totalSales = totalSales;
        dto.totalProducts = totalProducts;
        dto.bankDetails = null;
        LocalDateTime created = profile.getCreatedAt();
        dto.createdAt = created != null ? created.toString() : LocalDateTime.now().toString();
        dto.updatedAt = dto.createdAt;
        return dto;
    }

    /** Backend stores location as a free-text string (e.g. "Pune, Maharashtra"); split it best-effort. */
    private static LocationDto parseLocation(String raw) {
        if (raw == null || raw.isBlank()) {
            return new LocationDto("", "", "", "");
        }
        String[] parts = raw.split(",");
        String city = parts.length > 0 ? parts[0].trim() : raw.trim();
        String state = parts.length > 1 ? parts[1].trim() : "";
        return new LocationDto(raw.trim(), city, state, "");
    }

    // Getters (Jackson serialization)
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getFarmName() { return farmName; }
    public String getBio() { return bio; }
    public String getPhotoUrl() { return photoUrl; }
    public LocationDto getLocation() { return location; }
    public List<String> getCertifications() { return certifications; }
    public boolean isVerified() { return isVerified; }
    public boolean isAvailable() { return isAvailable; }
    public double getRating() { return rating; }
    public int getReviewCount() { return reviewCount; }
    public long getTotalSales() { return totalSales; }
    public long getTotalProducts() { return totalProducts; }
    public Object getBankDetails() { return bankDetails; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
}
