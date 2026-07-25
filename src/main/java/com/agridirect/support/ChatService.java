package com.agridirect.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ChatService {

    @Autowired
    private ChatRepository chatRepository;

    public List<ChatMessage> getMessages(UUID userId) {
        return chatRepository.findByUserIdOrderByCreatedAtAsc(userId);
    }

    public ChatMessage sendMessage(UUID userId, String content, boolean isFromUser) {
        ChatMessage msg = new ChatMessage(userId, isFromUser, content);
        ChatMessage saved = chatRepository.save(msg);
        
        // If message is from user, generate an automated response to simulate support
        if (isFromUser) {
            String autoReply = "Thanks for your message! An AgriDirect support agent will get back to you shortly.";
            if (content.toLowerCase().contains("refund")) {
                autoReply = "I see you're asking about a refund. You can request a return from your order details page if the order is delivered.";
            } else if (content.toLowerCase().contains("track") || content.toLowerCase().contains("where")) {
                autoReply = "You can track your order using the 'Live Track' button on your order details page once it is picked up.";
            }
            ChatMessage reply = new ChatMessage(userId, false, autoReply);
            chatRepository.save(reply);
        }
        
        return saved;
    }
}
