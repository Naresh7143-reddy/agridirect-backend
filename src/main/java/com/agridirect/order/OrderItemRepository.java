package com.agridirect.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    List<OrderItem> findByOrderId(UUID orderId);

    List<OrderItem> findByFarmerId(UUID farmerId);

    @Query("SELECT DISTINCT oi.orderId FROM OrderItem oi WHERE oi.farmerId = :farmerId")
    List<UUID> findDistinctOrderIdByFarmerId(UUID farmerId);
}
