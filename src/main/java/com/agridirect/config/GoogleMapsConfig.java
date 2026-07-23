package com.agridirect.config;

import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Google Maps API
 * Uses REST API instead of Java client library
 */
@Configuration
public class GoogleMapsConfig {
    // Google Maps API Key is configured via environment variable: GOOGLE_MAPS_API_KEY
    // No additional Spring beans needed as we're using Java's built-in HTTP client
}

