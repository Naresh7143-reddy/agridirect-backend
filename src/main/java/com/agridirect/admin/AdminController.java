package com.agridirect.admin;

import com.agridirect.common.dto.ApiResponse;
import com.agridirect.common.exception.ApiException;
import com.agridirect.farmer.FarmerProfile;
import com.agridirect.farmer.FarmerRepository;
import com.agridirect.order.Order;
import com.agridirect.order.OrderRepository;
import com.agridirect.product.ProductRepository;
import com.agridirect.user.User;
import com.agridirect.user.UserRepository;
import com.agridirect.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
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
}
