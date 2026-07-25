package com.agridirect.support;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatRepository extends JpaRepository<ChatMessage, UUID> {
    List<ChatMessage> findByUserIdOrderByCreatedAtAsc(UUID userId);
}
