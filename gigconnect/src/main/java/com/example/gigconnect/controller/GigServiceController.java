package com.example.gigconnect.controller;

import com.example.gigconnect.dto.PublicUserProfileDTO;
import com.example.gigconnect.model.GigService;
import com.example.gigconnect.service.GigServiceService;
import com.example.gigconnect.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class GigServiceController {

    @Autowired
    private GigServiceService gigServiceService;
     @Autowired
    private UserService userService; // Add this field
    @PostMapping
    public ResponseEntity<GigService> createService(@Valid @RequestBody GigService service, Authentication authentication) {
        GigService createdService = gigServiceService.createService(service, authentication.getName());
        return ResponseEntity.ok(createdService);
    }

    @GetMapping
    public ResponseEntity<List<GigService>> getAllServices() {
        return ResponseEntity.ok(gigServiceService.getAllServices());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GigService> getService(@PathVariable String id) {
        return ResponseEntity.ok(gigServiceService.getService(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GigService> updateService(@PathVariable String id, @Valid @RequestBody GigService service, Authentication authentication) {
        GigService updatedService = gigServiceService.updateService(id, service, authentication.getName());
        return ResponseEntity.ok(updatedService);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable String id, Authentication authentication) {
        gigServiceService.deleteService(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
    // GigServiceController.java
@GetMapping("/my-services")
public ResponseEntity<List<GigService>> getMyServices(Authentication authentication) {
    return ResponseEntity.ok(gigServiceService.getMyServices(authentication.getName()));
}  
// GigServiceController.java
@GetMapping("/search")
public ResponseEntity<List<PublicUserProfileDTO>> searchServices(
        @RequestParam String keyword,
        @RequestParam(required = false) String city,
        @RequestParam(required = false) String state,
        Authentication authentication) {
    if (authentication == null || authentication.getAuthorities().stream()
            .noneMatch(auth -> auth.getAuthority().equals("ROLE_CLIENT"))) {
        throw new RuntimeException("Only logged-in CLIENTs can search services");
    }
    List<PublicUserProfileDTO> profiles = userService.searchGigWorkers(keyword, city, state);
    return ResponseEntity.ok(profiles);
}

}