package com.example.gigconnect.controller;

import com.example.gigconnect.dto.PublicUserProfileDTO;
import com.example.gigconnect.model.GigService;
import com.example.gigconnect.service.GigServiceService;
import com.example.gigconnect.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class GigServiceController {

    private static final Logger logger = LoggerFactory.getLogger(GigServiceController.class);

    @Autowired
    private GigServiceService gigServiceService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<GigService> createService(@Valid @RequestBody GigService service, Authentication authentication) {
        try {
            GigService createdService = gigServiceService.createService(service, authentication.getName());
            return ResponseEntity.ok(createdService);
        } catch (RuntimeException e) {
            logger.error("Failed to create service: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping
    public ResponseEntity<List<GigService>> getAllServices() {
        try {
            return ResponseEntity.ok(gigServiceService.getAllServices());
        } catch (RuntimeException e) {
            logger.error("Failed to fetch all services: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<GigService> getService(@PathVariable String id) {
        try {
            return ResponseEntity.ok(gigServiceService.getService(id));
        } catch (RuntimeException e) {
            logger.error("Failed to fetch service with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<GigService> updateService(@PathVariable String id, @Valid @RequestBody GigService service, Authentication authentication) {
        try {
            GigService updatedService = gigServiceService.updateService(id, service, authentication.getName());
            return ResponseEntity.ok(updatedService);
        } catch (RuntimeException e) {
            logger.error("Failed to update service with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable String id, Authentication authentication) {
        try {
            gigServiceService.deleteService(id, authentication.getName());
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            logger.error("Failed to delete service with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }

    @GetMapping("/my-services")
    public ResponseEntity<List<GigService>> getMyServices(Authentication authentication) {
        try {
            return ResponseEntity.ok(gigServiceService.getMyServices(authentication.getName()));
        } catch (RuntimeException e) {
            logger.error("Failed to fetch my services for user {}: {}", authentication.getName(), e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }

@GetMapping("/search")
public ResponseEntity<List<PublicUserProfileDTO>> searchServices(
        @RequestParam String keyword,
        @RequestParam(required = false) String city,
        @RequestParam(required = false) String state,
        @RequestParam(required = false) List<String> skills,
        Authentication authentication) {
    logger.debug("Searching services with keyword: {}, city: {}, state: {}, skills: {}", keyword, city, state,
            skills);
    try {
        List<PublicUserProfileDTO> profiles = userService.searchGigWorkers(keyword, city, state, skills);
        return ResponseEntity.ok(profiles);
    } catch (RuntimeException e) {
        logger.error("Failed to search services: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
}
}