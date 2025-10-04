package com.example.gigconnect.repository;

import com.example.gigconnect.model.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    List<ChatMessage> findBySenderIdAndRecipientId(String senderId, String recipientId);
}