package com.agridirect.product;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    // Aligned with Product.isAvailable (column: is_available)
    List<Product> findByIsAvailableTrueAndIsDeletedFalse();

    List<Product> findByFarmerIdAndIsAvailableTrueAndIsDeletedFalse(UUID farmerId);

    List<Product> findByFarmerIdAndIsDeletedFalse(UUID farmerId);

    List<Product> findByCategoryIdAndIsAvailableTrueAndIsDeletedFalse(UUID categoryId);

    List<Product> findByNameContainingIgnoreCaseAndIsAvailableTrueAndIsDeletedFalse(String name);

    long countByFarmerIdAndIsAvailableTrueAndIsDeletedFalse(UUID farmerId);

    long countByFarmerIdAndIsDeletedFalse(UUID farmerId);

    List<Product> findByApprovalStatusAndIsDeletedFalse(String approvalStatus);

    /** Locks the product row for the duration of the transaction (prevents overselling). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") UUID id);
}
