package com.example.gigconnect.controller;

import com.example.gigconnect.model.HireRequest;
import com.example.gigconnect.service.HireRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/hire-requests")
public class HireRequestController {

    private static final Logger logger = LoggerFactory.getLogger(HireRequestController.class);

    @Autowired
    private HireRequestService hireRequestService;

    @PostMapping
    public ResponseEntity<HireRequest> createHireRequest(
            @Valid @RequestBody HireRequest hireRequest,
            Authentication authentication) {
        logger.debug("Received request to create hire request from user: {}", authentication.getName());
        HireRequest createdRequest = hireRequestService.createHireRequest(hireRequest, authentication.getName());
        logger.debug("Hire request created: {}", createdRequest);
        return ResponseEntity.ok(createdRequest);
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<HireRequest> acceptHireRequest(
            @PathVariable String id,
            Authentication authentication) {
        logger.debug("Received request to accept hire request {} by user: {}", id, authentication.getName());
        HireRequest updatedRequest = hireRequestService.acceptHireRequest(id, authentication.getName());
        logger.debug("Hire request accepted: {}", updatedRequest);
        return ResponseEntity.ok(updatedRequest);
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<HireRequest> rejectHireRequest(
            @PathVariable String id,
            Authentication authentication) {
        logger.debug("Received request to reject hire request {} by user: {}", id, authentication.getName());
        HireRequest updatedRequest = hireRequestService.rejectHireRequest(id, authentication.getName());
        logger.debug("Hire request rejected: {}", updatedRequest);
        return ResponseEntity.ok(updatedRequest);
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<HireRequest> completeHireRequest(
            @PathVariable String id,
            Authentication authentication) {
        logger.debug("Received request to complete hire request {} by user: {}", id, authentication.getName());
        HireRequest updatedRequest = hireRequestService.completeHireRequest(id, authentication.getName());
        logger.debug("Hire request completed: {}", updatedRequest);
        return ResponseEntity.ok(updatedRequest);
    }

    @GetMapping("/gig-worker")
    public ResponseEntity<List<HireRequest>> getHireRequestsForGigWorker(Authentication authentication) {
        logger.debug("Received request to fetch hire requests for gig worker: {}", authentication.getName());
        List<HireRequest> requests = hireRequestService.getHireRequestsForGigWorker(authentication.getName());
        logger.debug("Returning {} hire requests for gig worker", requests.size());
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/client")
    public ResponseEntity<List<HireRequest>> getHireRequestsForClient(Authentication authentication) {
        logger.debug("Received request to fetch hire requests for client: {}", authentication.getName());
        List<HireRequest> requests = hireRequestService.getHireRequestsForClient(authentication.getName());
        logger.debug("Returning {} hire requests for client", requests.size());
        return ResponseEntity.ok(requests);
    }
}