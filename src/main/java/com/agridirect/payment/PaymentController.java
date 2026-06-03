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
        UUID orderId = UUID.fromString((String) body.get("orderId"));
        Double amount = ((Number) body.get("amount")).doubleValue();
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
