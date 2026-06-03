package com.agridirect.delivery;

import com.agridirect.common.exception.ApiException;
import com.agridirect.notification.NotificationService;
import com.agridirect.order.Order;
import com.agridirect.order.OrderRepository;
import com.agridirect.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class DeliveryService {

    private static final Set<String> ALLOWED_STATUSES = Set.of("PICKED_UP", "ON_THE_WAY", "DELIVERED");

    @Autowired private DeliveryRepository deliveryRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private NotificationService notificationService;

    public DeliveryProfile getProfile(UUID userId) {
        return deliveryRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException("Delivery profile not found", HttpStatus.NOT_FOUND));
    }

    public DeliveryProfile updateAvailability(UUID userId, boolean available) {
        DeliveryProfile profile = getProfile(userId);
        profile.setAvailable(available);
        return deliveryRepository.save(profile);
    }

    public List<Order> getAssignedOrders(UUID agentId) {
        return orderRepository.findByDeliveryAgentIdOrderByCreatedAtDesc(agentId);
    }

    public Order updateOrderStatus(UUID agentId, UUID orderId, String status) {
        if (!ALLOWED_STATUSES.contains(status)) {
            throw new ApiException("Invalid status. Allowed: PICKED_UP, ON_THE_WAY, DELIVERED", HttpStatus.BAD_REQUEST);
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException("Order not found", HttpStatus.NOT_FOUND));
        if (!agentId.equals(order.getDeliveryAgentId())) {
            throw new ApiException("Order not assigned to this agent", HttpStatus.FORBIDDEN);
        }
        order.setStatus(status);
        Order saved = orderRepository.save(order);
        userRepository.findById(order.getBuyerId()).ifPresent(buyer -> {
            String token = buyer.getFcmToken();
            switch (status) {
                case "PICKED_UP"  -> notificationService.sendOrderPickedUp(token);
                case "DELIVERED"  -> notificationService.sendOrderDelivered(token);
                default           -> notificationService.sendToUser(token, "Order Update", "Your order status is now: " + status);
            }
        });
        return saved;
    }
}
