package com.agridirect.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByBuyerIdOrderByCreatedAtDesc(UUID buyerId);

    List<Order> findByDeliveryAgentIdOrderByCreatedAtDesc(UUID agentId);

    List<Order> findByStatus(String status);

    List<Order> findAllByOrderByCreatedAtDesc();

    java.util.Optional<Order> findByRazorpayOrderId(String razorpayOrderId);
}
