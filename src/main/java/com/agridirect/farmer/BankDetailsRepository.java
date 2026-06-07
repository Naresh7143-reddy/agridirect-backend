package com.agridirect.farmer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BankDetailsRepository extends JpaRepository<BankDetails, UUID> {

    Optional<BankDetails> findByUserId(UUID userId);
}
