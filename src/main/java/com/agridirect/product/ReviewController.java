package com.agridirect.product;

import com.agridirect.common.dto.ApiResponse;
import com.agridirect.product.dto.ReviewRequest;
import com.agridirect.product.dto.ReviewResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<ReviewResponse>> addReview(@Valid @RequestBody ReviewRequest request) {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID buyerId = UUID.fromString(userIdStr);
        return ResponseEntity.ok(ApiResponse.success(reviewService.addReview(buyerId, request)));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getProductReviews(@PathVariable UUID productId) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.getReviewsForProduct(productId)));
    }

    @GetMapping("/farmer/{farmerId}")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getFarmerReviews(@PathVariable UUID farmerId) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.getReviewsForFarmer(farmerId)));
    }
}
