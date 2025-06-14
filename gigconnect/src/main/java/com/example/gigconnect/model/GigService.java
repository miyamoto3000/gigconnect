package com.example.gigconnect.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Data
@Document(collection = "services")
public class GigService {
    @Id
    private String id;
    @NotBlank(message = "Title is required")
    private String title;
    @NotBlank(message = "Description is required")
    private String description;
    @Positive(message = "Price must be positive")
    private double price;
    @NotBlank(message = "Category is required")
    private String category;
    private String userId; // Links to GIG_WORKER, set by the service layer
}