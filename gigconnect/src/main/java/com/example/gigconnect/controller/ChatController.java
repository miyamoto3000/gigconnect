package com.example.gigconnect.controller;

import com.example.gigconnect.model.ChatMessage;
import com.example.gigconnect.model.User;
import com.example.gigconnect.repository.ChatMessageRepository;
import com.example.gigconnect.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller; // <-- IMPORTANT: Changed from @RestController
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody; // <-- IMPORTANT: Added this import

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller // <-- IMPORTANT: Changed from @RestController
public class ChatController {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage) {
        chatMessage.setTimestamp(LocalDateTime.now());
        chatMessageRepository.save(chatMessage);

        Optional<User> recipient = userRepository.findById(chatMessage.getRecipientId());
        
        if (recipient.isPresent()) {
            simpMessagingTemplate.convertAndSendToUser(
                recipient.get().getEmail(), 
                "/queue/messages", 
                chatMessage
            );
        } else {
            System.out.println("Cannot find recipient with ID: " + chatMessage.getRecipientId());
        }
    }

    @GetMapping("/api/messages/{senderId}/{recipientId}")
    @ResponseBody // <-- IMPORTANT: Added this to keep it a REST endpoint
    public List<ChatMessage> getChatHistory(@PathVariable String senderId, @PathVariable String recipientId) {
        // This is a simplified history fetch. A more robust implementation would
        // find messages where the sender/recipient are swapped as well.
        return chatMessageRepository.findBySenderIdAndRecipientId(senderId, recipientId);
    }
}