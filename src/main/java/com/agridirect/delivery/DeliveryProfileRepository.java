package com.agridirect.delivery;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for DeliveryProfile entity
 */
@Repository
public interface DeliveryProfileRepository extends JpaRepository<DeliveryProfile, UUID> {
    Optional<DeliveryProfile> findByUserId(UUID userId);
}
