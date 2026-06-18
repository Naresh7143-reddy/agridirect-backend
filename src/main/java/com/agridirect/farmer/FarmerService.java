package com.agridirect.farmer;

import com.agridirect.common.exception.ApiException;
import com.agridirect.order.Order;
import com.agridirect.order.OrderItem;
import com.agridirect.order.OrderItemRepository;
import com.agridirect.order.OrderRepository;
import com.agridirect.product.Product;
import com.agridirect.product.ProductRepository;
import com.agridirect.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class FarmerService {

    @Autowired private FarmerRepository farmerRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private BankDetailsRepository bankDetailsRepository;

    public FarmerProfile getProfile(UUID userId) {
        return farmerRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException("Farmer profile not found", HttpStatus.NOT_FOUND));
    }

    /** Builds the structured response DTO the mobile app expects (see FarmerProfileResponse). */
    public FarmerProfileResponse getProfileResponse(UUID userId) {
        FarmerProfile profile = getProfile(userId);
        long totalProducts = productRepository.findByFarmerId(userId).size();
        long totalSales = orderItemRepository.findByFarmerId(userId).stream()
                .mapToLong(i -> i.getQuantity() != null ? i.getQuantity().longValue() : 0L)
                .sum();
        return FarmerProfileResponse.from(profile, totalProducts, totalSales);
    }

    public FarmerProfile updateProfile(UUID userId, Map<String, Object> updates) {
        FarmerProfile profile = getProfile(userId);
        if (updates.get("farmName") != null)  profile.setFarmName((String) updates.get("farmName"));
        if (updates.get("location") != null)  profile.setLocation((String) updates.get("location"));
        if (updates.get("landAcres") != null) profile.setLandAcres(((Number) updates.get("landAcres")).doubleValue());
        return farmerRepository.save(profile);
    }

    public Map<String, Object> getDashboard(UUID userId) {
        List<Order> orders       = orderRepository.findByFarmerId(userId);
        List<OrderItem> items    = orderItemRepository.findByFarmerId(userId);
        long activeProducts      = productRepository.countByFarmerIdAndIsAvailableTrue(userId);
        long totalProducts       = productRepository.countByFarmerId(userId);
        long totalOrders         = orders.size();
        long pendingOrders       = orders.stream().filter(o -> "PENDING".equalsIgnoreCase(o.getStatus())).count();
        long acceptedOrders      = orders.stream().filter(o -> "ACCEPTED".equalsIgnoreCase(o.getStatus())).count();
        long deliveredOrders     = orders.stream().filter(o -> "DELIVERED".equalsIgnoreCase(o.getStatus())).count();
        double totalRevenue      = orders.stream()
                .filter(o -> "DELIVERED".equalsIgnoreCase(o.getStatus()))
                .mapToDouble(o -> o.getTotalAmount() != null ? o.getTotalAmount() : 0.0)
                .sum();
        double allOrdersValue    = orders.stream()
                .mapToDouble(o -> o.getTotalAmount() != null ? o.getTotalAmount() : 0.0)
                .sum();
        HashMap<String, Object> map = new HashMap<>();
        map.put("totalRevenue",    totalRevenue);
        map.put("totalEarnings",   allOrdersValue);
        map.put("totalOrders",     totalOrders);
        map.put("pendingOrders",   pendingOrders);
        map.put("acceptedOrders",  acceptedOrders);
        map.put("deliveredOrders", deliveredOrders);
        map.put("activeProducts",  activeProducts);
        map.put("totalProducts",   totalProducts);
        map.put("averageRating",   0.0);
        return map;
    }

    public BankDetails getBankDetails(UUID userId) {
        return bankDetailsRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException("Bank details not found", HttpStatus.NOT_FOUND));
    }

    public BankDetails saveBankDetails(UUID userId, Map<String, Object> body) {
        BankDetails details = bankDetailsRepository.findByUserId(userId).orElseGet(() -> {
            BankDetails b = new BankDetails();
            b.setUserId(userId);
            return b;
        });
        if (body.get("accountHolderName") != null) details.setAccountHolderName((String) body.get("accountHolderName"));
        if (body.get("accountNumber") != null)     details.setAccountNumber((String) body.get("accountNumber"));
        if (body.get("ifscCode") != null)           details.setIfscCode((String) body.get("ifscCode"));
        if (body.get("bankName") != null)           details.setBankName((String) body.get("bankName"));
        if (body.get("branchName") != null)         details.setBranchName((String) body.get("branchName"));
        if (body.get("upiId") != null)               details.setUpiId((String) body.get("upiId"));
        return bankDetailsRepository.save(details);
    }

    public FarmerProfile getPublicProfile(UUID farmerId) {
        return farmerRepository.findByUserId(farmerId)
                .orElseThrow(() -> new ApiException("Farmer not found", HttpStatus.NOT_FOUND));
    }

    public Product updateAvailability(UUID userId, UUID productId, boolean available) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiException("Product not found", HttpStatus.NOT_FOUND));
        if (!userId.equals(product.getFarmerId())) {
            throw new ApiException("Product does not belong to this farmer", HttpStatus.FORBIDDEN);
        }
        product.setAvailable(available);
        return productRepository.save(product);
    }

    public Map<String, Object> getEarnings(UUID userId) {
        List<OrderItem> items = orderItemRepository.findByFarmerId(userId);
        double totalEarnings = items.stream()
                .mapToDouble(i -> i.getPriceAtOrder() != null && i.getQuantity() != null
                        ? i.getPriceAtOrder() * i.getQuantity() : 0.0)
                .sum();
        return Map.of(
                "totalEarnings",     totalEarnings,
                "thisMonthEarnings", 0.0,
                "pendingPayout",     0.0
        );
    }
}
