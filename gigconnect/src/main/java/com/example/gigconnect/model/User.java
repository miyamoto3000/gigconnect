package com.example.gigconnect.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

@Data
@Document(collection = "users")
public class User {
    @Id
    private String id;
    @NotBlank(message = "Name is required")
    private String name;
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;
    @NotBlank(message = "Password is required")
    private String password;
    @NotBlank(message = "Role is required")
    private String role; // GIG_WORKER, CLIENT, ADMIN
    private String city; // e.g., Amravati, Solapur
    private String state; // e.g., Maharashtra
    private double[] location; // [longitude, latitude]
    private String[] skills; // For gig workers
    private PortfolioEntry[] portfolio; // Previous work
    private String[] mediaUrls; // Image URLs
    private List<Review> reviews; // List of reviews and ratings

    @Data
    public static class Review {
        private String clientId;
        private String clientName;
        private String comment;
        private int rating; // 1 to 5
    }
}