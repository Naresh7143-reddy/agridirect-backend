package com.agridirect.farmer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FarmerRepository extends JpaRepository<FarmerProfile, UUID> {

    Optional<FarmerProfile> findByUserId(UUID userId);

    List<FarmerProfile> findByVerifiedFalse();

    List<FarmerProfile> findAllByUserIdIn(java.util.Set<UUID> userIds);
}
