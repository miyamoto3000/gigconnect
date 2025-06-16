package com.example.gigconnect.service;

import com.example.gigconnect.model.GigService;
import com.example.gigconnect.model.User;
import com.example.gigconnect.repository.GigServiceRepository;
import com.example.gigconnect.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.validation.Valid;

import java.util.List;

@Service
public class GigServiceService {

    @Autowired
    private GigServiceRepository gigServiceRepository;

    @Autowired
    private UserRepository userRepository;

    public GigService createService(@Valid GigService service, String email) {
        User user = userRepository.findByEmail(email);
        if (user == null || !user.getRole().equals("GIG_WORKER")) {
            throw new RuntimeException("Only GIG_WORKERs can create services");
        }
        service.setUserId(user.getId());
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
        return gigServiceRepository.save(existingService);
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