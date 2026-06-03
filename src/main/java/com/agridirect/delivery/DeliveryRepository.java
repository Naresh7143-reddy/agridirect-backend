package com.agridirect.delivery;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeliveryRepository extends JpaRepository<DeliveryProfile, UUID> {

    Optional<DeliveryProfile> findByUserId(UUID userId);

    List<DeliveryProfile> findByIsAvailableTrue();
}
