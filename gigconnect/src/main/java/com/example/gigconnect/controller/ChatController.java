package com.example.gigconnect.controller;

import com.example.gigconnect.model.ChatMessage;
import com.example.gigconnect.model.User;
import com.example.gigconnect.repository.ChatMessageRepository;
import com.example.gigconnect.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

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
            // 5. Send the message to a user-specific destination using their ID
            String destination = "/user/" + chatMessage.getRecipientId() + "/queue/messages";
            simpMessagingTemplate.convertAndSend(destination, chatMessage);

        } else {
            System.out.println("Cannot find recipient with ID: " + chatMessage.getRecipientId());
        }
    }

    @GetMapping("/api/messages/{senderId}/{recipientId}")
    @ResponseBody
    public List<ChatMessage> getChatHistory(@PathVariable String senderId, @PathVariable String recipientId) {
        return chatMessageRepository.findBySenderIdAndRecipientId(senderId, recipientId);
    }
}