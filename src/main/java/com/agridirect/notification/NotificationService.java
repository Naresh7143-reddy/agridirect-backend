package com.agridirect.notification;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.logging.Logger;

@Service
public class NotificationService {

    private static final Logger log = Logger.getLogger(NotificationService.class.getName());

    public void sendToUser(String fcmToken, String title, String body) {
        if (fcmToken == null || fcmToken.isBlank()) {
            log.warning("FCM token is null or empty — skipping notification: " + title);
            return;
        }
        try {
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("FCM sent: " + response);
        } catch (Exception e) {
            log.severe("FCM send failed: " + e.getMessage());
        }
    }

    // Alias used by older callers
    public void sendToToken(String fcmToken, String title, String body) {
        sendToUser(fcmToken, title, body);
    }

    // Unused stub kept for compatibility
    public void sendOrderStatusUpdate(UUID buyerUserId, String orderId, String status) {
        log.info("sendOrderStatusUpdate called — use sendToUser directly with FCM token");
    }

    public void sendOrderPlaced(String farmerFcmToken, String productSummary) {
        sendToUser(farmerFcmToken, "New Order Received!",
                "You have a new order for " + productSummary + ". Open AgriDirect to accept.");
    }

    public void sendOrderAccepted(String buyerFcmToken) {
        sendToUser(buyerFcmToken, "Order Accepted!",
                "Your order has been accepted by the farmer and is being prepared.");
    }

    public void sendOrderPacked(String buyerFcmToken) {
        sendToUser(buyerFcmToken, "Order Packed!",
                "Your order is packed and ready for pickup by delivery agent.");
    }

    public void sendOrderPickedUp(String buyerFcmToken) {
        sendToUser(buyerFcmToken, "Order Picked Up!",
                "Your order has been picked up and is on the way to you!");
    }

    public void sendOrderDelivered(String buyerFcmToken) {
        sendToUser(buyerFcmToken, "Order Delivered!",
                "Your order has been delivered. Please rate your experience!");
    }

    public void sendPaymentConfirmed(String buyerFcmToken, Double amount) {
        sendToUser(buyerFcmToken, "Payment Confirmed!",
                "Payment of Rs " + amount + " confirmed. Your order is being processed.");
    }
}
