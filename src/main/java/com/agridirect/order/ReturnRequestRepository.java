package com.agridirect.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, UUID> {
    List<ReturnRequest> findByBuyerIdOrderByCreatedAtDesc(UUID buyerId);
    List<ReturnRequest> findByFarmerIdOrderByCreatedAtDesc(UUID farmerId);
}
