package com.example.gigconnect.dto;

import com.example.gigconnect.model.PortfolioEntry;
import lombok.Data;

@Data
public class UserProfileUpdateDTO {
    private String name;
    private String city;
    private String state;
    private double[] location;
    private String[] skills;
    private PortfolioEntry[] portfolio;
    private String[] mediaUrls;
    private Boolean openToWork;
}