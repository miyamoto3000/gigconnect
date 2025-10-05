// Create new file: src/main/java/com/example/gigconnect/model/Payment.java

package com.example.gigconnect.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "payments")
public class Payment {
    @Id
    private String id;
    private String hireRequestId;
    private String razorpayPaymentId;
    private String razorpayOrderId;
    private String razorpaySignature;
    private String status; // e.g., "success", "failed"
    private double amount;
    private LocalDateTime createdAt;
}