package com.agridirect.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findByIsAvailableTrue();

    List<Product> findByFarmerIdAndIsAvailableTrue(UUID farmerId);

    List<Product> findByFarmerId(UUID farmerId);

    List<Product> findByCategoryIdAndIsAvailableTrue(UUID categoryId);

    List<Product> findByNameContainingIgnoreCaseAndIsAvailableTrue(String name);

    long countByFarmerIdAndIsAvailableTrue(UUID farmerId);
}
