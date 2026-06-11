package com.agridirect.delivery;

import com.agridirect.common.exception.ApiException;
import com.agridirect.notification.NotificationService;
import com.agridirect.order.Order;
import com.agridirect.order.OrderRepository;
import com.agridirect.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class DeliveryService {

    private static final Set<String> ALLOWED_STATUSES = Set.of("PICKED_UP", "ON_THE_WAY", "DELIVERED");

    @Autowired private DeliveryRepository deliveryRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private NotificationService notificationService;
    @Autowired private com.agridirect.order.OrderService orderService;

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

    public List<Order> getAvailableOrders() {
        return orderService.getAvailableOrders();
    }

    public Order claimOrder(UUID agentId, UUID orderId) {
        return orderService.claimOrder(agentId, orderId);
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

    public Order getOrderById(UUID agentId, UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException("Order not found", HttpStatus.NOT_FOUND));
        if (!agentId.equals(order.getDeliveryAgentId())) {
            throw new ApiException("Order not assigned to this agent", HttpStatus.FORBIDDEN);
        }
        return order;
    }

    public Order confirmOrder(UUID agentId, UUID orderId) {
        Order order = getOrderById(agentId, orderId);
        order.setStatus("DELIVERED");
        Order saved = orderRepository.save(order);
        userRepository.findById(order.getBuyerId()).ifPresent(buyer ->
                notificationService.sendOrderDelivered(buyer.getFcmToken()));
        return saved;
    }

    public DeliveryProfile updatePhoto(UUID userId, String url) {
        DeliveryProfile profile = getProfile(userId);
        profile.setPhotoUrl(url);
        return deliveryRepository.save(profile);
    }

    public DeliveryProfile updateLocation(UUID userId, Double lat, Double lng) {
        DeliveryProfile profile = getProfile(userId);
        profile.setCurrentLat(lat);
        profile.setCurrentLng(lng);
        return deliveryRepository.save(profile);
    }

    public DeliveryProfile updateProfile(UUID userId, java.util.Map<String, Object> updates) {
        DeliveryProfile profile = getProfile(userId);
        if (updates.get("vehicleType") != null)   profile.setVehicleType((String) updates.get("vehicleType"));
        if (updates.get("vehicleNumber") != null) profile.setLicenseNo((String) updates.get("vehicleNumber"));
        if (updates.get("licenseNumber") != null) profile.setLicenseNo((String) updates.get("licenseNumber"));
        return deliveryRepository.save(profile);
    }

    public Map<String, Object> getEarnings(UUID agentId) {
        List<Order> delivered = orderRepository.findByDeliveryAgentIdOrderByCreatedAtDesc(agentId)
                .stream().filter(o -> "DELIVERED".equals(o.getStatus())).toList();
        double total = delivered.stream()
                .mapToDouble(o -> o.getTotalAmount() != null ? o.getTotalAmount() * 0.05 : 0.0)
                .sum();
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("pending", 0.0);
        result.put("paid", total);
        result.put("today", 0.0);
        result.put("thisWeek", 0.0);
        result.put("thisMonth", total);
        result.put("byDate", List.of());
        result.put("totalDeliveries", delivered.size());
        return result;
    }
}
