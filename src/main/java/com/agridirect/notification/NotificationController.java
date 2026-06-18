package com.agridirect.notification;

import com.agridirect.common.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired private UserNotificationRepository repo;

    /** Get all notifications for the authenticated user. */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<UserNotification>>> getNotifications() {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        return ResponseEntity.ok(ApiResponse.success(repo.findByUserIdOrderByCreatedAtDesc(userId)));
    }

    /** Unread count badge. */
    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount() {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        long count = repo.countByUserIdAndReadFalse(userId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("count", count)));
    }

    /** Mark a single notification read. */
    @PatchMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> markRead(@PathVariable UUID id) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        repo.findById(id).ifPresent(n -> {
            if (userId.equals(n.getUserId())) {
                n.setRead(true);
                repo.save(n);
            }
        });
        return ResponseEntity.ok(ApiResponse.success("Marked read", null));
    }

    /** Mark all notifications read. */
    @PostMapping("/mark-all-read")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> markAllRead() {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        repo.markAllReadByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("All marked read", null));
    }
}
