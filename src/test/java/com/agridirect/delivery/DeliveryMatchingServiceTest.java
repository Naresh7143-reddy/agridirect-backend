package com.agridirect.delivery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Delivery Matching Service Tests")
@ExtendWith(MockitoExtension.class)
class DeliveryMatchingServiceTest {

    @InjectMocks
    private DeliveryMatchingService matchingService;

    @Mock
    private DeliveryPartnerRepository partnerRepository;

    @Mock
    private MapsService mapsService;

    @BeforeEach
    void setUp() {
        // @InjectMocks handles injection; ensure delivery radius config values are set
        ReflectionTestUtils.setField(matchingService, "searchRadiusKm", 5.0);
    }

    @Test
    @DisplayName("Should check availability status")
    void testCheckAvailability() {
        // Arrange
        List<DeliveryPartner> partners = new ArrayList<>();
        DeliveryPartner partner1 = new DeliveryPartner();
        partner1.setId("partner1");
        partner1.setAvgRating(4.5);
        partner1.setCurrentOrdersCount(1);
        partner1.setMaxConcurrentOrders(3);
        partners.add(partner1);

        when(partnerRepository.findNearbyAvailablePartners(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(partners);

        // Act
        DeliveryMatchingService.DeliveryAvailabilityStatus status =
                matchingService.checkAvailability(12.9716, 77.5946);

        // Assert
        assertTrue(status.getIsAvailable());
        assertEquals(1, status.getAvailablePartnersCount());
        assertEquals(4.5, status.getAvgRating());
        assertTrue(status.getEstimatedWaitMinutes() > 0);
    }

    @Test
    @DisplayName("Should return low availability when no partners found")
    void testLowAvailability() {
        // Arrange
        when(partnerRepository.findNearbyAvailablePartners(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(new ArrayList<>());

        // Act
        DeliveryMatchingService.DeliveryAvailabilityStatus status =
                matchingService.checkAvailability(12.9716, 77.5946);

        // Assert
        assertFalse(status.getIsAvailable());
        assertEquals(0, status.getAvailablePartnersCount());
        assertEquals("LOW", status.getAvailabilityStatus());
    }

    @Test
    @DisplayName("Should check partner availability")
    void testIsPartnerAvailable() {
        // Arrange
        DeliveryPartner partner = new DeliveryPartner();
        partner.setId("partner1");
        partner.setIsAvailable(true);
        partner.setIsActive(true);
        partner.setVerificationStatus("VERIFIED");
        partner.setCurrentOrdersCount(1);
        partner.setMaxConcurrentOrders(3);
        partner.setCurrentLatitude(12.9716);
        partner.setCurrentLongitude(77.5946);
        partner.setLastLocationUpdate(System.currentTimeMillis());

        when(mapsService.getHaversineDistance(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(2.0);

        // Act
        boolean available = matchingService.isPartnerAvailable(partner, 12.9352, 77.6245,
                System.currentTimeMillis());

        // Assert
        assertTrue(available);
    }

    @Test
    @DisplayName("Should reject unavailable partner")
    void testRejectUnavailablePartner() {
        // Arrange
        DeliveryPartner partner = new DeliveryPartner();
        partner.setId("partner1");
        partner.setIsAvailable(false); // Not available
        partner.setIsActive(true);
        partner.setVerificationStatus("VERIFIED");

        // Act
        boolean available = matchingService.isPartnerAvailable(partner, 12.9352, 77.6245,
                System.currentTimeMillis());

        // Assert
        assertFalse(available);
    }

    @Test
    @DisplayName("Should update partner location")
    void testUpdatePartnerLocation() {
        // Arrange
        DeliveryPartner partner = new DeliveryPartner();
        partner.setId("partner1");
        partner.setCurrentLatitude(12.9716);
        partner.setCurrentLongitude(77.5946);

        when(partnerRepository.findById("partner1")).thenReturn(Optional.of(partner));
        when(partnerRepository.save(any(DeliveryPartner.class))).thenReturn(partner);

        // Act
        matchingService.updatePartnerLocation("partner1", 13.0827, 77.6063);

        // Assert
        verify(partnerRepository, times(1)).save(any(DeliveryPartner.class));
    }

    @Test
    @DisplayName("Should decrement partner order count")
    void testDecrementOrderCount() {
        // Arrange
        DeliveryPartner partner = new DeliveryPartner();
        partner.setId("partner1");
        partner.setCurrentOrdersCount(2);

        when(partnerRepository.findById("partner1")).thenReturn(Optional.of(partner));
        when(partnerRepository.save(any(DeliveryPartner.class))).thenReturn(partner);

        // Act
        matchingService.decrementPartnerOrderCount("partner1");

        // Assert
        verify(partnerRepository, times(1)).save(any(DeliveryPartner.class));
    }
}
