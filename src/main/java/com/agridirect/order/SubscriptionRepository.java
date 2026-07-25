package com.agridirect.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    List<Subscription> findByBuyerIdOrderByCreatedAtDesc(UUID buyerId);
    List<Subscription> findByFarmerIdOrderByCreatedAtDesc(UUID farmerId);
}
