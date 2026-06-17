package com.agridirect.payment;

import com.agridirect.common.dto.ApiResponse;
import com.agridirect.order.Order;
import com.agridirect.order.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired private PaymentService paymentService;
    @Autowired private OrderRepository orderRepository;
    @Autowired private PaymentRepository paymentRepository;

    @PostMapping("/create-order")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createOrder(@RequestBody Map<String, Object> body) {
        String buyerId = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID orderId = UUID.fromString((String) body.get("orderId"));
        // Ownership check: verify the order belongs to this buyer
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new com.agridirect.common.exception.ApiException("Order not found", org.springframework.http.HttpStatus.NOT_FOUND));
        if (!order.getBuyerId().toString().equals(buyerId)) {
            return ResponseEntity.status(403).body(ApiResponse.error("Not your order"));
        }
        // Use server-computed total — never trust the client-supplied amount
        Double amount = order.getTotalAmount();
        if (amount == null || amount <= 0) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid order amount"));
        }
        return ResponseEntity.ok(ApiResponse.success(paymentService.createRazorpayOrder(orderId, amount)));
    }

    @PostMapping("/verify")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<Void>> verifyPayment(@RequestBody Map<String, String> body) {
        boolean valid = paymentService.verifyPayment(
                body.get("razorpayOrderId"),
                body.get("razorpayPaymentId"),
                body.get("signature"));
        if (valid) {
            return ResponseEntity.ok(ApiResponse.success("Payment verified successfully", null));
        }
        return ResponseEntity.badRequest().body(ApiResponse.error("Payment verification failed"));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {
        paymentService.handleWebhook(payload, signature);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{paymentId}")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<Payment>> getPaymentById(@PathVariable UUID paymentId) {
        String buyerId = SecurityContextHolder.getContext().getAuthentication().getName();
        Payment payment = paymentService.getPaymentById(paymentId);
        // IDOR guard: verify the payment's order belongs to the requesting buyer
        Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new com.agridirect.common.exception.ApiException("Order not found", org.springframework.http.HttpStatus.NOT_FOUND));
        if (!order.getBuyerId().toString().equals(buyerId)) {
            return ResponseEntity.status(403).body(ApiResponse.error("Access denied"));
        }
        return ResponseEntity.ok(ApiResponse.success(payment));
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<Payment>> getPaymentByOrder(@PathVariable UUID orderId) {
        String buyerId = SecurityContextHolder.getContext().getAuthentication().getName();
        // IDOR guard: verify the order belongs to the requesting buyer
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new com.agridirect.common.exception.ApiException("Order not found", org.springframework.http.HttpStatus.NOT_FOUND));
        if (!order.getBuyerId().toString().equals(buyerId)) {
            return ResponseEntity.status(403).body(ApiResponse.error("Access denied"));
        }
        return ResponseEntity.ok(ApiResponse.success(paymentService.getPaymentByOrderId(orderId)));
    }

    @PostMapping("/{paymentId}/refund")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> refund(@PathVariable UUID paymentId, @RequestBody(required = false) Map<String, Object> body) {
        String buyerId = SecurityContextHolder.getContext().getAuthentication().getName();
        Payment payment = paymentService.getPaymentById(paymentId);
        // IDOR guard
        Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new com.agridirect.common.exception.ApiException("Order not found", org.springframework.http.HttpStatus.NOT_FOUND));
        if (!order.getBuyerId().toString().equals(buyerId)) {
            return ResponseEntity.status(403).body(ApiResponse.error("Access denied"));
        }
        // Cap refund at the original paid amount — never trust a client-supplied amount
        Double maxRefundable = payment.getAmount();
        Double amount = maxRefundable; // default: full refund
        if (body != null && body.get("amount") != null) {
            double requested = ((Number) body.get("amount")).doubleValue();
            if (requested <= 0 || requested > maxRefundable) {
                return ResponseEntity.badRequest().body(ApiResponse.error(
                        "Refund amount must be between 0 and " + maxRefundable));
            }
            amount = requested;
        }
        String reason = (body != null && body.get("reason") != null) ? (String) body.get("reason") : null;
        Payment refunded = paymentService.initiateRefund(paymentId, amount, reason);
        Map<String, Object> result = Map.of(
                "refundId", refunded.getRefundId(),
                "status", refunded.getRefundStatus(),
                "amount", refunded.getRefundAmount(),
                "currency", refunded.getCurrency()
        );
        return ResponseEntity.ok(ApiResponse.success("Refund initiated", result));
    }

    @GetMapping("/refunds/{refundId}")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRefundStatus(@PathVariable String refundId) {
        Payment payment = paymentService.getRefundById(refundId);
        Map<String, Object> result = Map.of(
                "refundId", payment.getRefundId(),
                "status", payment.getRefundStatus(),
                "amount", payment.getRefundAmount()
        );
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<List<Payment>>> getHistory() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID buyerId = UUID.fromString(userId);
        List<UUID> orderIds = orderRepository.findByBuyerIdOrderByCreatedAtDesc(buyerId)
                .stream().map(Order::getId).collect(Collectors.toList());
        List<Payment> payments = paymentRepository.findByOrderIdIn(orderIds);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }
}
