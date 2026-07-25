package com.agridirect.product;

import com.agridirect.product.dto.ReviewRequest;
import com.agridirect.product.dto.ReviewResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    public ReviewResponse addReview(UUID buyerId, ReviewRequest request) {
        Review review = Review.builder()
                .productId(request.getProductId())
                .buyerId(buyerId)
                .farmerId(request.getFarmerId())
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        Review savedReview = reviewRepository.save(review);
        return new ReviewResponse(savedReview);
    }

    public List<ReviewResponse> getReviewsForProduct(UUID productId) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId)
                .stream()
                .map(ReviewResponse::new)
                .collect(Collectors.toList());
    }

    public List<ReviewResponse> getReviewsForFarmer(UUID farmerId) {
        return reviewRepository.findByFarmerIdOrderByCreatedAtDesc(farmerId)
                .stream()
                .map(ReviewResponse::new)
                .collect(Collectors.toList());
    }
}
