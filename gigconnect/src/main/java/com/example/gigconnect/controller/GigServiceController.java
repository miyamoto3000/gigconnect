package com.example.gigconnect.controller;

import com.example.gigconnect.model.GigService;
import com.example.gigconnect.service.GigServiceService;
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
}