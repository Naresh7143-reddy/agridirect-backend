package com.agridirect.payment;

import com.agridirect.common.exception.ApiException;
import com.agridirect.notification.NotificationService;
import com.agridirect.order.Order;
import com.agridirect.order.OrderItem;
import com.agridirect.order.OrderItemRepository;
import com.agridirect.order.OrderRepository;
import com.agridirect.user.UserRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    @Value("${razorpay.key-id}")
    private String keyId;

    @Value("${razorpay.key-secret}")
    private String keySecret;

    @Value("${razorpay.webhook-secret}")
    private String webhookSecret;

    @Autowired private PaymentRepository paymentRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private NotificationService notificationService;

    @Transactional
    public Map<String, Object> createRazorpayOrder(UUID agridirectOrderId, Double amount) {
        try {
            RazorpayClient client = new RazorpayClient(keyId, keySecret);

            JSONObject options = new JSONObject();
            options.put("amount", (int) (amount * 100)); // paise
            options.put("currency", "INR");
            options.put("receipt", "receipt_" + agridirectOrderId.toString().substring(0, 8));

            com.razorpay.Order rzpOrder = client.orders.create(options);
            String rzpOrderId = rzpOrder.get("id");

            // Save payment record
            paymentRepository.save(Payment.builder()
                    .orderId(agridirectOrderId)
                    .razorpayOrderId(rzpOrderId)
                    .amount(amount)
                    .currency("INR")
                    .status("CREATED")
                    .build());

            // Update our order with the Razorpay order id
            orderRepository.findById(agridirectOrderId).ifPresent(order -> {
                order.setRazorpayOrderId(rzpOrderId);
                orderRepository.save(order);
            });

            Map<String, Object> result = new HashMap<>();
            result.put("razorpay_order_id", rzpOrderId);
            result.put("amount", amount);
            result.put("currency", "INR");
            result.put("key_id", keyId);
            return result;

        } catch (com.razorpay.RazorpayException e) {
            throw new ApiException("Failed to create Razorpay order: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public boolean verifyPayment(String razorpayOrderId, String razorpayPaymentId, String signature) {
        try {
            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_order_id", razorpayOrderId);
            attributes.put("razorpay_payment_id", razorpayPaymentId);
            attributes.put("razorpay_signature", signature);

            boolean valid = Utils.verifyPaymentSignature(attributes, keySecret);

            if (valid) {
                // Update payment record
                paymentRepository.findByRazorpayOrderId(razorpayOrderId).ifPresent(payment -> {
                    payment.setStatus("PAID");
                    payment.setRazorpayPaymentId(razorpayPaymentId);
                    paymentRepository.save(payment);
                });

                // Update order
                Order order = orderRepository.findByRazorpayOrderId(razorpayOrderId).orElse(null);

                if (order != null) {
                    order.setStatus("PAID");
                    order.setPaymentStatus("PAID");
                    order.setRazorpayPaymentId(razorpayPaymentId);
                    orderRepository.save(order);

                    // Notify buyer
                    double paidAmount = order.getTotalAmount() != null ? order.getTotalAmount() : 0.0;
                    userRepository.findById(order.getBuyerId()).ifPresent(buyer ->
                        notificationService.sendPaymentConfirmed(buyer.getFcmToken(), paidAmount));

                    // Notify farmers
                    List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
                    Set<UUID> farmerIds = items.stream().map(OrderItem::getFarmerId).collect(Collectors.toSet());
                    for (UUID farmerId : farmerIds) {
                        userRepository.findById(farmerId).ifPresent(farmer -> {
                            if (farmer.getFcmToken() != null) {
                                notificationService.sendToToken(farmer.getFcmToken(),
                                        "New Paid Order",
                                        "You have a new paid order!");
                            }
                        });
                    }
                }
            }

            return valid;

        } catch (com.razorpay.RazorpayException e) {
            return false;
        }
    }

    public void handleWebhook(String payload, String signature) {
        try {
            boolean valid = Utils.verifyWebhookSignature(payload, signature, webhookSecret);
            if (!valid) return;

            JSONObject event = new JSONObject(payload);
            String eventType = event.getString("event");

            if ("payment.captured".equals(eventType)) {
                String rzpOrderId = event
                        .getJSONObject("payload")
                        .getJSONObject("payment")
                        .getJSONObject("entity")
                        .getString("order_id");

                orderRepository.findByRazorpayOrderId(rzpOrderId).ifPresent(order -> {
                    if (!"PAID".equals(order.getStatus())) {
                        order.setStatus("PAID");
                        order.setPaymentStatus("PAID");
                        orderRepository.save(order);
                    }
                });

            } else if ("payment.failed".equals(eventType)) {
                String rzpOrderId = event
                        .getJSONObject("payload")
                        .getJSONObject("payment")
                        .getJSONObject("entity")
                        .getString("order_id");

                orderRepository.findByRazorpayOrderId(rzpOrderId).ifPresent(order -> {
                    order.setPaymentStatus("FAILED");
                    orderRepository.save(order);
                });
            }

        } catch (Exception e) {
            System.err.println("Webhook processing error: " + e.getMessage());
        }
    }

    public Payment getPaymentByOrderId(UUID orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ApiException("Payment not found for order", HttpStatus.NOT_FOUND));
    }

    public Payment getPaymentById(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ApiException("Payment not found", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Payment initiateRefund(UUID paymentId, Double amount, String reason) {
        Payment payment = getPaymentById(paymentId);
        if (!"PAID".equals(payment.getStatus())) {
            throw new ApiException("Only paid payments can be refunded", HttpStatus.BAD_REQUEST);
        }
        String refundId = "rfnd_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        payment.setRefundId(refundId);
        payment.setRefundStatus("PENDING");
        payment.setRefundAmount(amount != null ? amount : payment.getAmount());
        payment.setRefundReason(reason);
        return paymentRepository.save(payment);
    }

    public Payment getRefundById(String refundId) {
        return paymentRepository.findByRefundId(refundId)
                .orElseThrow(() -> new ApiException("Refund not found", HttpStatus.NOT_FOUND));
    }
}
