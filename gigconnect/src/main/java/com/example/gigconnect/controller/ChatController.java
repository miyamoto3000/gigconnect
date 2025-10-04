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
// miyamoto3000/gigconnect/gigconnect-2378d3ac891c84131f680da8adb53f8ecf22c62f/gigconnect/src/main/java/com/example/gigconnect/controller/ChatController.java

@Autowired
private SimpMessagingTemplate simpMessagingTemplate;

@Autowired
private ChatMessageRepository chatMessageRepository;

@Autowired
private UserRepository userRepository;

// In ChatController.java

@MessageMapping("/chat.sendMessage")
public void sendMessage(@Payload ChatMessage chatMessage, org.springframework.security.core.Authentication authentication) {
    // 1. Get UserDetails from the Authentication object
    org.springframework.security.core.userdetails.UserDetails userDetails = 
        (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();
    
    // 2. Look up the sender by their email (which is the username in UserDetails)
    User sender = userRepository.findByEmail(userDetails.getUsername());
    if (sender == null) { return; }

    chatMessage.setSenderId(sender.getId());
    chatMessage.setTimestamp(LocalDateTime.now());
    
    // 3. Save the message to the database
    chatMessageRepository.save(chatMessage);

    // 4. Find the recipient user
    Optional<User> recipient = userRepository.findById(chatMessage.getRecipientId());
    
    if (recipient.isPresent()) {
        String recipientEmail = recipient.get().getEmail();
        
        // 5. Send the message using the recipient's email
        simpMessagingTemplate.convertAndSendToUser(
            recipientEmail,
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