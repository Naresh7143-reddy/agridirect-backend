package com.agridirect.support;

import com.agridirect.common.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/support")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @GetMapping("/messages")
    @PreAuthorize("hasAnyRole('BUYER', 'FARMER')")
    public ResponseEntity<ApiResponse<List<ChatMessage>>> getMessages() {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = UUID.fromString(userIdStr);
        return ResponseEntity.ok(ApiResponse.success(chatService.getMessages(userId)));
    }

    @PostMapping("/messages")
    @PreAuthorize("hasAnyRole('BUYER', 'FARMER')")
    public ResponseEntity<ApiResponse<ChatMessage>> sendMessage(@RequestBody Map<String, String> request) {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = UUID.fromString(userIdStr);
        String content = request.get("content");
        
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Content cannot be empty"));
        }
        
        return ResponseEntity.ok(ApiResponse.success(chatService.sendMessage(userId, content, true)));
    }
}
