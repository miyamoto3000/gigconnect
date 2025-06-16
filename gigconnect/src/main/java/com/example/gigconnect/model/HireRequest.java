package com.example.gigconnect.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

// HireRequest.java
@Data
@Document(collection = "hire_requests")
public class HireRequest {
    @Id
    private String id;
    @NotBlank(message = "Service ID is required")
    private String serviceId;
    private String gigWorkerId;
    private String clientId;
    private String message;
    private String status; // PENDING, ACCEPTED, REJECTED
    private String workStatus; // IN_PROGRESS, COMPLETED (for accepted requests)
    private String createdAt;
    private String requestedDateTime;
    @Positive(message = "Budget must be positive")
    private double budget;

    @Override
    public String toString() {
        return "HireRequest{" +
                "id='" + id + '\'' +
                ", serviceId='" + serviceId + '\'' +
                ", gigWorkerId='" + gigWorkerId + '\'' +
                ", clientId='" + clientId + '\'' +
                ", message='" + message + '\'' +
                ", status='" + status + '\'' +
                ", workStatus='" + workStatus + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", requestedDateTime='" + requestedDateTime + '\'' +
                ", budget=" + budget +
                '}';
    }
}