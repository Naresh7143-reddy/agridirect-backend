package com.agridirect.delivery;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryTrackingRepository extends JpaRepository<DeliveryTracking, String> {
    
    Optional<DeliveryTracking> findByOrderId(String orderId);
    
    List<DeliveryTracking> findByDeliveryPartnerId(String deliveryPartnerId);
    
    List<DeliveryTracking> findByDeliveryPartnerIdAndStatus(String deliveryPartnerId, String status);
    
    List<DeliveryTracking> findByStatus(String status);
    
    @Query("SELECT dt FROM DeliveryTracking dt WHERE dt.deliveryPartnerId = :partnerId " +
           "AND dt.status IN ('IN_TRANSIT', 'NEAR_DELIVERY')")
    List<DeliveryTracking> findActiveDeliveriesForPartner(@Param("partnerId") String partnerId);
    
    @Query("SELECT COUNT(dt) FROM DeliveryTracking dt WHERE dt.status = 'DELIVERED' " +
           "AND dt.deliveryPartnerId = :partnerId AND dt.deliveredAt >= :since")
    Integer countDeliveriesForPartnerSince(@Param("partnerId") String partnerId, @Param("since") Long since);
}
