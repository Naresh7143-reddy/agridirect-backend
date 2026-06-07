package com.agridirect.buyer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, UUID> {

    List<Wishlist> findByBuyerId(UUID buyerId);

    Optional<Wishlist> findByBuyerIdAndProductId(UUID buyerId, UUID productId);

    boolean existsByBuyerIdAndProductId(UUID buyerId, UUID productId);

    void deleteByBuyerIdAndProductId(UUID buyerId, UUID productId);
}
