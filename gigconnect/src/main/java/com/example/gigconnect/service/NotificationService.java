package com.example.gigconnect.service;

import com.example.gigconnect.model.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void sendNotification(Notification notification) {
        // Send the notification to a user-specific topic, e.g., /topic/notifications/USER_ID
        messagingTemplate.convertAndSend("/topic/notifications/" + notification.getToUserId(), notification);
    }
}