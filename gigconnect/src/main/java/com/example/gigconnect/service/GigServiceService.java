package com.example.gigconnect.service;

import com.example.gigconnect.model.GigService;
import com.example.gigconnect.model.User;
import com.example.gigconnect.repository.GigServiceRepository;
import com.example.gigconnect.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.validation.Valid; 

import org.springframework.web.client.RestTemplate; // <-- ADD
import org.slf4j.Logger; // <-- ADD
import org.slf4j.LoggerFactory; // <-- ADD
import java.util.List; // <-- ADD
import java.util.Map;

import java.util.List;

@Service
public class GigServiceService {

    @Autowired
    private GigServiceRepository gigServiceRepository;

    @Autowired
    private UserRepository userRepository; 

    @Autowired
    private RestTemplate restTemplate; 
    private final String vectorizerUrl = "http://localhost:5001/vectorize";
    private static final Logger logger = LoggerFactory.getLogger(GigServiceService.class);

    public GigService createService(@Valid GigService service, String email) {
        User user = userRepository.findByEmail(email);
        if (user == null || !user.getRole().equals("GIG_WORKER")) {
            throw new RuntimeException("Only GIG_WORKERs can create services");
        }
        service.setUserId(user.getId()); 

        List<Double> vector = getVectorForService(service.getTitle(), service.getDescription());
        service.setServiceVector(vector); 

        return gigServiceRepository.save(service);
    }

    public GigService getService(String id) {
        return gigServiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found"));
    }

    public List<GigService> getAllServices() {
        return gigServiceRepository.findAll();
    }

    public GigService updateService(String id, @Valid GigService updatedService, String email) {
        GigService existingService = getService(id);
        User user = userRepository.findByEmail(email);
        if (user == null || !user.getId().equals(existingService.getUserId())) {
            throw new RuntimeException("Not authorized to update this service");
        }
        existingService.setTitle(updatedService.getTitle());
        existingService.setDescription(updatedService.getDescription());
        existingService.setPrice(updatedService.getPrice());
        existingService.setCategory(updatedService.getCategory()); 
        List<Double> vector = getVectorForService(updatedService.getTitle(), updatedService.getDescription());
        existingService.setServiceVector(vector);
        return gigServiceRepository.save(existingService);
    } 
    private List<Double> getVectorForService(String title, String description) {
        try {
            String combinedText = title + ". " + description;
            Map<String, String> requestBody = Map.of("text", combinedText);
            
            // Call the Python service
            Map<String, Object> response = restTemplate.postForObject(vectorizerUrl, requestBody, Map.class);
            
            if (response != null && response.containsKey("vector")) {
                return (List<Double>) response.get("vector");
            }
        } catch (Exception e) {
            logger.error("Failed to generate vector for service: " + e.getMessage());
        }
        return null; // Don't fail the operation if vectorizing fails
    }

    public void deleteService(String id, String email) {
        GigService service = getService(id);
        User user = userRepository.findByEmail(email);
        if (user == null || !user.getId().equals(service.getUserId())) {
            throw new RuntimeException("Not authorized to delete this service");
        }
        gigServiceRepository.delete(service);
    } 
    // GigServiceService.java
public List<GigService> getMyServices(String email) {
    User user = userRepository.findByEmail(email);
    if (user == null || !user.getRole().equals("GIG_WORKER")) {
        throw new RuntimeException("Only GIG_WORKERs can view their services");
    }
    return gigServiceRepository.findByUserId(user.getId());
}
}