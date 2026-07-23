package com.agridirect.delivery;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryPartnerRepository extends JpaRepository<DeliveryPartner, String> {
    
    Optional<DeliveryPartner> findByUserId(String userId);
    
    Optional<DeliveryPartner> findByPhone(String phone);
    
    List<DeliveryPartner> findByIsAvailableAndIsActiveAndVerificationStatusAndCurrentOrdersCountLessThan(
            Boolean isAvailable, Boolean isActive, String verificationStatus, Integer maxOrders);
    
    @Query(value = "SELECT * FROM delivery_partners WHERE is_available = true " +
                   "AND is_active = true AND verification_status = 'VERIFIED' " +
                   "AND current_orders_count < max_concurrent_orders " +
                   "AND (6371 * acos(cos(radians(:latitude)) * cos(radians(current_latitude)) * " +
                   "cos(radians(current_longitude) - radians(:longitude)) + " +
                   "sin(radians(:latitude)) * sin(radians(current_latitude)))) <= :radiusKm " +
                   "ORDER BY avg_rating DESC, current_orders_count ASC", nativeQuery = true)
    List<DeliveryPartner> findNearbyAvailablePartners(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("radiusKm") Double radiusKm);
    
    @Query("SELECT COUNT(dp) FROM DeliveryPartner dp WHERE dp.isAvailable = true " +
           "AND dp.isActive = true AND dp.verificationStatus = 'VERIFIED'")
    Integer countAvailablePartners();
    
    List<DeliveryPartner> findByVerificationStatus(String verificationStatus);
    
    List<DeliveryPartner> findByIsActiveAndVerificationStatus(Boolean isActive, String verificationStatus);
}
