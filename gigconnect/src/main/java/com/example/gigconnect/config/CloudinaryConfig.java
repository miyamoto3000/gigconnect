package com.example.gigconnect.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.url}")
    private String cloudinaryUrl;

    @Bean
    public Cloudinary getCloudinary() {
        return new Cloudinary(cloudinaryUrl);
    } 
   
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}