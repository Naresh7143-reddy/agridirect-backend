package com.agridirect.order;

import com.agridirect.order.dto.SubscriptionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SubscriptionService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    public Subscription createSubscription(UUID buyerId, SubscriptionRequest request) {
        Subscription subscription = Subscription.builder()
                .buyerId(buyerId)
                .productId(request.getProductId())
                .farmerId(request.getFarmerId())
                .frequency(request.getFrequency())
                .quantity(request.getQuantity())
                .deliveryAddress(request.getDeliveryAddress())
                .status("ACTIVE")
                .build();
        return subscriptionRepository.save(subscription);
    }

    public List<Subscription> getBuyerSubscriptions(UUID buyerId) {
        return subscriptionRepository.findByBuyerIdOrderByCreatedAtDesc(buyerId);
    }

    public Subscription cancelSubscription(UUID subscriptionId, UUID buyerId) {
        Optional<Subscription> opt = subscriptionRepository.findById(subscriptionId);
        if (opt.isEmpty() || !opt.get().getBuyerId().equals(buyerId)) {
            throw new RuntimeException("Subscription not found or unauthorized");
        }
        Subscription sub = opt.get();
        sub.setStatus("CANCELLED");
        return subscriptionRepository.save(sub);
    }
}
