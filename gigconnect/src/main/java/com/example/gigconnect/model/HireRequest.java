package com.example.gigconnect.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;

@Data
@Document(collection = "hire_requests")
public class HireRequest {
    @Id
    private String id;
    @NotBlank(message = "Service ID is required")
    private String serviceId; // The GigService being hired for
    private String gigWorkerId; // The GIG_WORKER providing the service
    private String clientId; // The CLIENT making the request
    private String message; // Optional message from the CLIENT
    private String status; // PENDING, ACCEPTED, REJECTED, COMPLETED
    private String createdAt; // Timestamp of creation
     private String requestedDateTime; 

      @Override
    public String toString() {
        return "HireRequest{" +
                "id='" + id + '\'' +
                ", serviceId='" + serviceId + '\'' +
                ", gigWorkerId='" + gigWorkerId + '\'' +
                ", clientId='" + clientId + '\'' +
                ", message='" + message + '\'' +
                ", status='" + status + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", requestedDateTime='" + requestedDateTime + '\'' +
                '}';
    }
}