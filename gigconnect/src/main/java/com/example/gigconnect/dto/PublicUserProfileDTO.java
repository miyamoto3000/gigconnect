package com.example.gigconnect.dto;

import com.example.gigconnect.model.PortfolioEntry;
import com.example.gigconnect.model.GigService;
import lombok.Data;

import java.util.List;

@Data
public class PublicUserProfileDTO {
    private String id;
    private String name;
    private String city;
    private String state;
    private String[] skills;
    private PortfolioEntry[] portfolio;
    private String[] mediaUrls;
    private double averageRating;
    private List<ReviewDTO> reviews;
    private List<GigService> services;

    @Data
    public static class ReviewDTO {
        private String comment;
        private int rating;
        private String clientId;
        private String clientName;
    }
}