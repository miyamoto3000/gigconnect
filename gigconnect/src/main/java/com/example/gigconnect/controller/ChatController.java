package com.example.gigconnect.controller;

import com.example.gigconnect.model.ChatMessage;
import com.example.gigconnect.model.User;
import com.example.gigconnect.repository.ChatMessageRepository;
import com.example.gigconnect.repository.UserRepository; // Import UserRepository
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
public class ChatController {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository; // Inject UserRepository

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage) {
        chatMessage.setTimestamp(LocalDateTime.now());
        chatMessageRepository.save(chatMessage);

        // --- UPDATED LOGIC ---
        // Find the recipient user to get their email (which is their principal name)
        Optional<User> recipient = userRepository.findById(chatMessage.getRecipientId());
        
        if (recipient.isPresent()) {
            // Use the recipient's email to send the message
            simpMessagingTemplate.convertAndSendToUser(
                recipient.get().getEmail(), 
                "/queue/messages", 
                chatMessage
            );
        } else {
            // Handle case where recipient is not found, maybe log an error
            System.out.println("Cannot find recipient with ID: " + chatMessage.getRecipientId());
        }
    }

    @GetMapping("/api/messages/{senderId}/{recipientId}")
    public List<ChatMessage> getChatHistory(@PathVariable String senderId, @PathVariable String recipientId) {
        // This is a simplified history fetch. A more robust implementation would
        // find messages where the sender/recipient are swapped as well.
        return chatMessageRepository.findBySenderIdAndRecipientId(senderId, recipientId);
    }
}