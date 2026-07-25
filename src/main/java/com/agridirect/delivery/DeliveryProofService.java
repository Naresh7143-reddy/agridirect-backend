package com.agridirect.delivery;

import com.agridirect.order.Order;
import com.agridirect.order.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class DeliveryProofService {

    @Autowired
    private DeliveryProofRepository deliveryProofRepository;

    @Autowired
    private OrderRepository orderRepository;

    public DeliveryProof verifyOtp(UUID orderId, UUID deliveryAgentId, String otp) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new RuntimeException("Order not found");
        }
        
        Order order = orderOpt.get();
        if (!otp.equals(order.getDeliveryOtp())) {
            throw new RuntimeException("Invalid OTP");
        }

        order.setStatus("DELIVERED");
        orderRepository.save(order);

        DeliveryProof proof = DeliveryProof.builder()
                .orderId(orderId)
                .deliveryAgentId(deliveryAgentId)
                .proofType("OTP")
                .otpProvided(otp)
                .verified(true)
                .build();
                
        return deliveryProofRepository.save(proof);
    }
}
