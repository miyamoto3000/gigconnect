package com.example.gigconnect.model;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class Notification {
    private String content;
    private String toUserId;
}