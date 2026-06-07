package com.agridirect.buyer;

import com.agridirect.common.exception.ApiException;
import com.agridirect.order.Order;
import com.agridirect.order.OrderRepository;
import com.agridirect.product.Product;
import com.agridirect.product.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BuyerService {

    @Autowired private BuyerRepository buyerRepository;
    @Autowired private AddressRepository addressRepository;
    @Autowired private WishlistRepository wishlistRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private ProductRepository productRepository;

    public BuyerProfile getProfile(UUID userId) {
        return buyerRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException("Buyer profile not found", HttpStatus.NOT_FOUND));
    }

    public BuyerProfile updateProfile(UUID userId, Map<String, Object> updates) {
        BuyerProfile profile = getProfile(userId);
        if (updates.get("buyerType") != null)  profile.setBuyerType((String) updates.get("buyerType"));
        if (updates.get("address") != null)    profile.setAddress((String) updates.get("address"));
        if (updates.get("gstNumber") != null)  profile.setGstNumber((String) updates.get("gstNumber"));
        return buyerRepository.save(profile);
    }

    // ── Addresses ─────────────────────────────────────────────────────────────

    public List<Address> getAddresses(UUID buyerId) {
        return addressRepository.findByBuyerId(buyerId);
    }

    @Transactional
    public Address addAddress(UUID buyerId, Map<String, Object> body) {
        Address address = new Address();
        address.setBuyerId(buyerId);
        applyAddressFields(address, body);

        boolean setAsDefault = Boolean.TRUE.equals(body.get("setAsDefault"));
        List<Address> existing = addressRepository.findByBuyerId(buyerId);
        if (setAsDefault || existing.isEmpty()) {
            existing.forEach(a -> a.setDefault(false));
            addressRepository.saveAll(existing);
            address.setDefault(true);
        }
        return addressRepository.save(address);
    }

    @Transactional
    public Address updateAddress(UUID buyerId, UUID addressId, Map<String, Object> body) {
        Address address = addressRepository.findByIdAndBuyerId(addressId, buyerId)
                .orElseThrow(() -> new ApiException("Address not found", HttpStatus.NOT_FOUND));
        applyAddressFields(address, body);
        return addressRepository.save(address);
    }

    @Transactional
    public void deleteAddress(UUID buyerId, UUID addressId) {
        Address address = addressRepository.findByIdAndBuyerId(addressId, buyerId)
                .orElseThrow(() -> new ApiException("Address not found", HttpStatus.NOT_FOUND));
        addressRepository.delete(address);
    }

    @Transactional
    public void setDefaultAddress(UUID buyerId, UUID addressId) {
        Address target = addressRepository.findByIdAndBuyerId(addressId, buyerId)
                .orElseThrow(() -> new ApiException("Address not found", HttpStatus.NOT_FOUND));
        List<Address> all = addressRepository.findByBuyerId(buyerId);
        all.forEach(a -> a.setDefault(a.getId().equals(addressId)));
        addressRepository.saveAll(all);
        target.setDefault(true);
        addressRepository.save(target);
    }

    private void applyAddressFields(Address address, Map<String, Object> body) {
        if (body.get("label") != null)   address.setLabel((String) body.get("label"));
        if (body.get("line1") != null)   address.setLine1((String) body.get("line1"));
        if (body.get("line2") != null)   address.setLine2((String) body.get("line2"));
        if (body.get("city") != null)    address.setCity((String) body.get("city"));
        if (body.get("state") != null)   address.setState((String) body.get("state"));
        if (body.get("pincode") != null) address.setPincode((String) body.get("pincode"));
        if (body.get("lat") != null)     address.setLat(((Number) body.get("lat")).doubleValue());
        if (body.get("lng") != null)     address.setLng(((Number) body.get("lng")).doubleValue());
    }

    // ── Orders ────────────────────────────────────────────────────────────────

    public Map<String, Object> trackOrder(UUID buyerId, UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException("Order not found", HttpStatus.NOT_FOUND));
        if (!buyerId.equals(order.getBuyerId())) {
            throw new ApiException("Order does not belong to this buyer", HttpStatus.FORBIDDEN);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", order.getId());
        result.put("status", order.getStatus());
        result.put("paymentStatus", order.getPaymentStatus());
        result.put("deliveryAgentId", order.getDeliveryAgentId());
        result.put("deliveryAddress", order.getDeliveryAddress());
        result.put("updatedAt", order.getUpdatedAt());
        result.put("timeline", List.of(
                Map.of("status", "PENDING", "label", "Order Placed", "completed", true),
                Map.of("status", "PAID", "label", "Payment Confirmed", "completed", isAtLeast(order.getStatus(), "PAID")),
                Map.of("status", "PACKED", "label", "Packed", "completed", isAtLeast(order.getStatus(), "PACKED")),
                Map.of("status", "PICKED_UP", "label", "Picked Up", "completed", isAtLeast(order.getStatus(), "PICKED_UP")),
                Map.of("status", "ON_THE_WAY", "label", "On The Way", "completed", isAtLeast(order.getStatus(), "ON_THE_WAY")),
                Map.of("status", "DELIVERED", "label", "Delivered", "completed", isAtLeast(order.getStatus(), "DELIVERED"))
        ));
        return result;
    }

    private boolean isAtLeast(String current, String target) {
        List<String> order = List.of("PENDING", "PAID", "PACKED", "PICKED_UP", "ON_THE_WAY", "DELIVERED");
        int curIdx = order.indexOf(current);
        int tgtIdx = order.indexOf(target);
        return curIdx >= 0 && tgtIdx >= 0 && curIdx >= tgtIdx;
    }

    @Transactional
    public void rateOrder(UUID buyerId, UUID orderId, Map<String, Object> body) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException("Order not found", HttpStatus.NOT_FOUND));
        if (!buyerId.equals(order.getBuyerId())) {
            throw new ApiException("Order does not belong to this buyer", HttpStatus.FORBIDDEN);
        }
        // Minimal persistence — append rating/review to notes for record-keeping (no dedicated rating table).
        Object rating = body.get("rating");
        Object review = body.get("review");
        String entry = "[Rating: " + rating + (review != null ? ", Review: " + review : "") + "]";
        order.setNotes(order.getNotes() != null ? order.getNotes() + " " + entry : entry);
        orderRepository.save(order);
    }

    // ── Wishlist ──────────────────────────────────────────────────────────────

    public List<Map<String, Object>> getWishlist(UUID buyerId) {
        List<Wishlist> items = wishlistRepository.findByBuyerId(buyerId);
        return items.stream().map(w -> {
            Map<String, Object> entry = new HashMap<>();
            entry.put("id", w.getId());
            entry.put("buyerId", w.getBuyerId());
            entry.put("addedAt", w.getAddedAt());
            Product product = productRepository.findById(w.getProductId()).orElse(null);
            entry.put("product", product);
            return entry;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void addToWishlist(UUID buyerId, UUID productId) {
        if (wishlistRepository.existsByBuyerIdAndProductId(buyerId, productId)) {
            return;
        }
        productRepository.findById(productId)
                .orElseThrow(() -> new ApiException("Product not found", HttpStatus.NOT_FOUND));
        Wishlist wishlist = new Wishlist();
        wishlist.setBuyerId(buyerId);
        wishlist.setProductId(productId);
        wishlistRepository.save(wishlist);
    }

    @Transactional
    public void removeFromWishlist(UUID buyerId, UUID productId) {
        wishlistRepository.deleteByBuyerIdAndProductId(buyerId, productId);
    }

    public boolean isInWishlist(UUID buyerId, UUID productId) {
        return wishlistRepository.existsByBuyerIdAndProductId(buyerId, productId);
    }
}
