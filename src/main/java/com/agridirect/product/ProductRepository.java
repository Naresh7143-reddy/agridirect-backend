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

    List<Product> findByIsAvailableTrue();

    List<Product> findByFarmerIdAndIsAvailableTrue(UUID farmerId);

    List<Product> findByFarmerId(UUID farmerId);

    List<Product> findByCategoryIdAndIsAvailableTrue(UUID categoryId);

    List<Product> findByNameContainingIgnoreCaseAndIsAvailableTrue(String name);

    long countByFarmerIdAndIsAvailableTrue(UUID farmerId);

    long countByFarmerId(UUID farmerId);

    List<Product> findByApprovalStatus(String approvalStatus);

    /** Locks the product row for the duration of the transaction (prevents overselling). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") UUID id);
}
