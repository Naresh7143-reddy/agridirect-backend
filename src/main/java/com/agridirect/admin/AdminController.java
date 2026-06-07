package com.agridirect.admin;

import com.agridirect.category.Category;
import com.agridirect.category.CategoryService;
import com.agridirect.common.dto.ApiResponse;
import com.agridirect.common.exception.ApiException;
import com.agridirect.farmer.FarmerProfile;
import com.agridirect.farmer.FarmerRepository;
import com.agridirect.notification.Notification;
import com.agridirect.notification.NotificationRepository;
import com.agridirect.notification.NotificationService;
import com.agridirect.order.Order;
import com.agridirect.order.OrderRepository;
import com.agridirect.product.Product;
import com.agridirect.product.ProductRepository;
import com.agridirect.storage.CloudinaryService;
import com.agridirect.user.User;
import com.agridirect.user.UserRepository;
import com.agridirect.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private FarmerRepository farmerRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryService categoryService;
    @Autowired private CloudinaryService cloudinaryService;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private NotificationService notificationService;

    private static final Set<String> ALLOWED_ORDER_STATUSES = Set.of(
            "PENDING", "PAID", "PACKED", "PICKED_UP", "ON_THE_WAY", "DELIVERED", "CANCELLED");

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers()));
    }

    @GetMapping("/users/role/{role}")
    public ResponseEntity<ApiResponse<List<User>>> getUsersByRole(@PathVariable String role) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUsersByRole(role)));
    }

    @PutMapping("/users/{id}/block")
    public ResponseEntity<ApiResponse<Void>> blockUser(@PathVariable String id) {
        try {
            userService.blockUser(UUID.fromString(id));
        } catch (IllegalArgumentException e) {
            throw new ApiException("Invalid user ID", HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(ApiResponse.success("User blocked successfully", null));
    }

    @PutMapping("/users/{id}/unblock")
    public ResponseEntity<ApiResponse<Void>> unblockUser(@PathVariable String id) {
        try {
            userService.unblockUser(UUID.fromString(id));
        } catch (IllegalArgumentException e) {
            throw new ApiException("Invalid user ID", HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(ApiResponse.success("User unblocked successfully", null));
    }

    @GetMapping("/farmers/pending")
    public ResponseEntity<ApiResponse<List<FarmerProfile>>> getPendingFarmers() {
        return ResponseEntity.ok(ApiResponse.success(farmerRepository.findByVerifiedFalse()));
    }

    @PutMapping("/farmers/{farmerId}/verify")
    public ResponseEntity<ApiResponse<Void>> verifyFarmer(@PathVariable String farmerId) {
        UUID id;
        try {
            id = UUID.fromString(farmerId);
        } catch (IllegalArgumentException e) {
            throw new ApiException("Invalid farmer ID", HttpStatus.BAD_REQUEST);
        }
        FarmerProfile profile = farmerRepository.findByUserId(id)
                .orElseThrow(() -> new ApiException("Farmer profile not found", HttpStatus.NOT_FOUND));
        profile.setVerified(true);
        farmerRepository.save(profile);
        return ResponseEntity.ok(ApiResponse.success("Farmer verified successfully", null));
    }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<Order>>> getAllOrders() {
        return ResponseEntity.ok(ApiResponse.success(orderRepository.findAllByOrderByCreatedAtDesc()));
    }

    @GetMapping("/analytics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAnalytics() {
        long totalUsers    = userRepository.count();
        long totalFarmers  = userRepository.findByRole("FARMER").size();
        long totalBuyers   = userRepository.findByRole("BUYER").size();
        long totalOrders   = orderRepository.count();
        long totalProducts = productRepository.count();
        long delivered     = orderRepository.findByStatus("DELIVERED").size();

        Map<String, Object> analytics = Map.of(
                "totalUsers",      totalUsers,
                "totalFarmers",    totalFarmers,
                "totalBuyers",     totalBuyers,
                "totalOrders",     totalOrders,
                "totalProducts",   totalProducts,
                "deliveredOrders", delivered
        );
        return ResponseEntity.ok(ApiResponse.success(analytics));
    }

    // ── Users ─────────────────────────────────────────────────────────────────

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable String id) {
        UUID userId;
        try {
            userId = UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new ApiException("Invalid user ID", HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(ApiResponse.success(userService.findById(userId)));
    }

    // ── Farmer verification ───────────────────────────────────────────────────

    @PutMapping("/farmers/{farmerId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectFarmer(@PathVariable String farmerId, @RequestBody(required = false) Map<String, String> body) {
        UUID id;
        try {
            id = UUID.fromString(farmerId);
        } catch (IllegalArgumentException e) {
            throw new ApiException("Invalid farmer ID", HttpStatus.BAD_REQUEST);
        }
        FarmerProfile profile = farmerRepository.findByUserId(id)
                .orElseThrow(() -> new ApiException("Farmer profile not found", HttpStatus.NOT_FOUND));
        profile.setVerified(false);
        farmerRepository.save(profile);
        return ResponseEntity.ok(ApiResponse.success("Farmer rejected", null));
    }

    // ── Products ──────────────────────────────────────────────────────────────

    @GetMapping("/products/pending")
    public ResponseEntity<ApiResponse<List<Product>>> getPendingProducts() {
        return ResponseEntity.ok(ApiResponse.success(productRepository.findByApprovalStatus("PENDING")));
    }

    @PutMapping("/products/{productId}/approve")
    public ResponseEntity<ApiResponse<Product>> approveProduct(@PathVariable UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiException("Product not found", HttpStatus.NOT_FOUND));
        product.setApprovalStatus("APPROVED");
        return ResponseEntity.ok(ApiResponse.success("Product approved", productRepository.save(product)));
    }

    @PutMapping("/products/{productId}/reject")
    public ResponseEntity<ApiResponse<Product>> rejectProduct(@PathVariable UUID productId, @RequestBody(required = false) Map<String, String> body) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiException("Product not found", HttpStatus.NOT_FOUND));
        product.setApprovalStatus("REJECTED");
        product.setAvailable(false);
        return ResponseEntity.ok(ApiResponse.success("Product rejected", productRepository.save(product)));
    }

    // ── Orders ────────────────────────────────────────────────────────────────

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<ApiResponse<Order>> updateOrderStatus(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if (status == null || !ALLOWED_ORDER_STATUSES.contains(status)) {
            throw new ApiException("Invalid status", HttpStatus.BAD_REQUEST);
        }
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ApiException("Order not found", HttpStatus.NOT_FOUND));
        order.setStatus(status);
        return ResponseEntity.ok(ApiResponse.success("Order status updated", orderRepository.save(order)));
    }

    // ── Categories ────────────────────────────────────────────────────────────

    @PutMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<Category>> updateCategory(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        String imageUrl = (String) body.get("imageUrl");
        Boolean isActive = body.get("isActive") != null ? (Boolean) body.get("isActive") : null;
        return ResponseEntity.ok(ApiResponse.success("Category updated", categoryService.updateCategory(id, name, imageUrl, isActive)));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Category deleted", null));
    }

    @PostMapping("/categories/{id}/image")
    public ResponseEntity<ApiResponse<String>> uploadCategoryImage(@PathVariable UUID id, @RequestParam MultipartFile file) throws Exception {
        String url = cloudinaryService.uploadProductImage(file);
        categoryService.updateCategoryImage(id, url);
        return ResponseEntity.ok(ApiResponse.success("Category image updated", url));
    }

    // ── Reports ───────────────────────────────────────────────────────────────

    @GetMapping("/reports/{type}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getReport(@PathVariable String type) {
        Map<String, Object> report = new HashMap<>();
        report.put("type", type);
        report.put("generatedAt", java.time.LocalDateTime.now().toString());
        switch (type) {
            case "revenue", "orders" -> {
                report.put("totalOrders", orderRepository.count());
                report.put("totalRevenue", orderRepository.findAll().stream()
                        .mapToDouble(o -> o.getTotalAmount() != null ? o.getTotalAmount() : 0.0).sum());
            }
            case "users" -> {
                report.put("totalUsers", userRepository.count());
                report.put("totalFarmers", userRepository.findByRole("FARMER").size());
                report.put("totalBuyers", userRepository.findByRole("BUYER").size());
            }
            case "products" -> {
                report.put("totalProducts", productRepository.count());
                report.put("availableProducts", productRepository.findByIsAvailableTrue().size());
            }
            case "deliveries" -> {
                report.put("totalDelivered", orderRepository.findByStatus("DELIVERED").size());
            }
            default -> report.put("data", "No data available for report type: " + type);
        }
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    // ── Notifications ─────────────────────────────────────────────────────────

    @PostMapping("/notifications")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendNotification(@RequestBody Map<String, Object> body) {
        String title = (String) body.get("title");
        String text = (String) body.get("body");
        String imageUrl = (String) body.get("imageUrl");

        @SuppressWarnings("unchecked")
        List<String> userIds = (List<String>) body.get("userIds");
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) body.get("roles");

        List<User> targets;
        if (userIds != null && !userIds.isEmpty()) {
            targets = userIds.stream()
                    .map(idStr -> userRepository.findById(UUID.fromString(idStr)).orElse(null))
                    .filter(java.util.Objects::nonNull)
                    .collect(java.util.stream.Collectors.toList());
        } else if (roles != null && !roles.isEmpty()) {
            targets = roles.stream()
                    .flatMap(role -> userRepository.findByRole(role).stream())
                    .collect(java.util.stream.Collectors.toList());
        } else {
            targets = userRepository.findAll();
        }

        int sent = 0;
        for (User user : targets) {
            if (user.getFcmToken() != null && !user.getFcmToken().isBlank()) {
                notificationService.sendToUser(user.getFcmToken(), title, text);
                sent++;
            }
        }

        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setBody(text);
        notification.setImageUrl(imageUrl);
        notification.setRole(roles != null && !roles.isEmpty() ? String.join(",", roles) : null);
        notification.setSentCount(sent);
        notificationRepository.save(notification);

        return ResponseEntity.ok(ApiResponse.success("Notification sent", Map.of("sent", sent)));
    }
}
