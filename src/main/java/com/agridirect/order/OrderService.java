package com.agridirect.order;

import com.agridirect.common.exception.ApiException;
import com.agridirect.notification.NotificationService;
import com.agridirect.order.dto.OrderItemRequest;
import com.agridirect.order.dto.OrderRequest;
import com.agridirect.product.Product;
import com.agridirect.product.ProductRepository;
import com.agridirect.product.ProductService;
import com.agridirect.user.User;
import com.agridirect.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductService productService;
    @Autowired private UserRepository userRepository;
    @Autowired private NotificationService notificationService;

    @Transactional
    public Order placeOrder(UUID buyerId, OrderRequest req) {
        // 1. Validate all items
        for (OrderItemRequest item : req.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ApiException(
                            "Product not found: " + item.getProductId(), HttpStatus.BAD_REQUEST));
            if (!product.isAvailable()) {
                throw new ApiException("Product not available: " + product.getName(), HttpStatus.BAD_REQUEST);
            }
            if (product.getStockQuantity() != null && product.getStockQuantity() < item.getQuantity()) {
                throw new ApiException(
                        "Insufficient stock for: " + product.getName() +
                        " (available: " + product.getStockQuantity() + ")", HttpStatus.BAD_REQUEST);
            }
        }

        // 2. Calculate total
        double total = 0.0;
        for (OrderItemRequest item : req.getItems()) {
            Product product = productRepository.findById(item.getProductId()).get();
            total += product.getPrice() * item.getQuantity();
        }

        // 3 & 4. Build and save order
        Order order = orderRepository.save(Order.builder()
                .buyerId(buyerId)
                .status("PENDING")
                .totalAmount(total)
                .deliveryAddress(req.getDeliveryAddress())
                .notes(req.getNotes())
                .paymentStatus("PENDING")
                .build());

        // 5. Save order items and collect unique farmerIds
        Set<UUID> farmerIds = new java.util.HashSet<>();
        for (OrderItemRequest item : req.getItems()) {
            Product product = productRepository.findById(item.getProductId()).get();
            orderItemRepository.save(OrderItem.builder()
                    .orderId(order.getId())
                    .productId(product.getId())
                    .farmerId(product.getFarmerId())
                    .productName(product.getName())
                    .quantity(item.getQuantity())
                    .unit(product.getUnit())
                    .priceAtOrder(product.getPrice())
                    .build());
            farmerIds.add(product.getFarmerId());

            // 6. Deduct stock
            productService.updateStock(product.getId(), item.getQuantity());
        }

        // 7. Notify each farmer
        String productSummary = req.getItems().stream()
                .map(item -> productRepository.findById(item.getProductId())
                        .map(Product::getName).orElse("item"))
                .collect(Collectors.joining(", "));
        for (UUID farmerId : farmerIds) {
            userRepository.findById(farmerId).ifPresent(farmer ->
                notificationService.sendOrderPlaced(farmer.getFcmToken(), productSummary));
        }

        return order;
    }

    public Order getOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException("Order not found", HttpStatus.NOT_FOUND));
    }

    public List<Order> getBuyerOrders(UUID buyerId) {
        return orderRepository.findByBuyerIdOrderByCreatedAtDesc(buyerId);
    }

    public List<Order> getFarmerOrders(UUID farmerId) {
        List<UUID> orderIds = orderItemRepository.findDistinctOrderIdByFarmerId(farmerId);
        if (orderIds.isEmpty()) return List.of();
        return orderIds.stream()
                .map(id -> orderRepository.findById(id))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toList());
    }

    /**
     * Orders that are packed by farmers but not yet assigned to any delivery
     * agent. Every available delivery agent sees this list and can claim one.
     * Swiggy/Zomato-style open marketplace.
     */
    public List<Order> getAvailableOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(o -> "PACKED".equals(o.getStatus()) && o.getDeliveryAgentId() == null)
                .collect(Collectors.toList());
    }

    /** Delivery agent self-claims an order from the available pool. */
    @Transactional
    public Order claimOrder(UUID agentId, UUID orderId) {
        Order order = getOrderById(orderId);
        if (order.getDeliveryAgentId() != null) {
            throw new ApiException("Order already claimed by another agent", HttpStatus.CONFLICT);
        }
        if (!"PACKED".equals(order.getStatus())) {
            throw new ApiException("Only packed orders can be claimed", HttpStatus.BAD_REQUEST);
        }
        order.setDeliveryAgentId(agentId);
        order.setStatus("ASSIGNED");
        Order saved = orderRepository.save(order);
        userRepository.findById(order.getBuyerId()).ifPresent(buyer ->
            notificationService.sendToUser(buyer.getFcmToken(),
                    "Delivery partner assigned",
                    "A delivery partner is picking up your order."));
        return saved;
    }

    @Transactional
    public Order acceptOrder(UUID farmerId, UUID orderId) {
        Order order = getOrderById(orderId);
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        boolean hasFarmerItems = items.stream().anyMatch(i -> farmerId.equals(i.getFarmerId()));
        if (!hasFarmerItems) {
            throw new ApiException("No items in this order belong to you", HttpStatus.FORBIDDEN);
        }
        order.setStatus("ACCEPTED");
        return orderRepository.save(order);
    }

    @Transactional
    public Order markPacked(UUID farmerId, UUID orderId) {
        Order order = getOrderById(orderId);
        order.setStatus("PACKED");
        Order saved = orderRepository.save(order);
        userRepository.findById(order.getBuyerId()).ifPresent(buyer ->
            notificationService.sendOrderPacked(buyer.getFcmToken()));
        return saved;
    }

    @Transactional
    public Order cancelOrder(UUID buyerId, UUID orderId) {
        Order order = getOrderById(orderId);
        if (!buyerId.equals(order.getBuyerId())) {
            throw new ApiException("Not your order", HttpStatus.FORBIDDEN);
        }
        Set<String> cancellableStatuses = Set.of("PENDING", "PAID");
        if (!cancellableStatuses.contains(order.getStatus())) {
            throw new ApiException("Cannot cancel order in current status", HttpStatus.BAD_REQUEST);
        }
        order.setStatus("CANCELLED");
        userRepository.findById(buyerId).ifPresent(buyer ->
            notificationService.sendToUser(buyer.getFcmToken(), "Order Cancelled",
                    "Your order has been cancelled successfully."));

        // Restore stock
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        for (OrderItem item : items) {
            productRepository.findById(item.getProductId()).ifPresent(product -> {
                double restored = (product.getStockQuantity() != null ? product.getStockQuantity() : 0.0)
                        + item.getQuantity();
                product.setStockQuantity(restored);
                product.setAvailable(true);
                productRepository.save(product);
            });
        }

        return orderRepository.save(order);
    }

    @Transactional
    public Order assignDeliveryAgent(UUID orderId, UUID agentId) {
        Order order = getOrderById(orderId);
        order.setDeliveryAgentId(agentId);
        order.setStatus("PACKED");
        return orderRepository.save(order);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }
}
