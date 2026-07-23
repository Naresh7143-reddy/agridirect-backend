package com.agridirect.delivery;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, String> {
    
    Optional<Location> findByUserIdAndIsPrimaryTrue(String userId);
    
    List<Location> findByUserId(String userId);
    
    List<Location> findByUserIdAndIsActiveTrue(String userId);
    
    Optional<Location> findByUserIdAndLocationType(String userId, String locationType);
    
    @Query(value = "SELECT * FROM locations WHERE location_type = :locationType AND is_active = true", nativeQuery = true)
    List<Location> findActiveByLocationType(@Param("locationType") String locationType);
}
