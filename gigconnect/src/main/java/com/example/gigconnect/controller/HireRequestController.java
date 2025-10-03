package com.example.gigconnect.controller;

import com.example.gigconnect.model.HireRequest;
import com.example.gigconnect.service.HireRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
        try {
            HireRequest createdRequest = hireRequestService.createHireRequest(hireRequest, authentication.getName());
            logger.debug("Hire request created: {}", createdRequest);
            return ResponseEntity.ok(createdRequest);
        } catch (RuntimeException e) {
            logger.error("Failed to create hire request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<HireRequest> acceptHireRequest(
            @PathVariable String id,
            Authentication authentication) {
        logger.debug("Received request to accept hire request {} by user: {}", id, authentication.getName());
        try {
            HireRequest updatedRequest = hireRequestService.acceptHireRequest(id, authentication.getName());
            logger.debug("Hire request accepted: {}", updatedRequest);
            return ResponseEntity.ok(updatedRequest);
        } catch (RuntimeException e) {
            logger.error("Failed to accept hire request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<HireRequest> rejectHireRequest(
            @PathVariable String id,
            Authentication authentication) {
        logger.debug("Received request to reject hire request {} by user: {}", id, authentication.getName());
        try {
            HireRequest updatedRequest = hireRequestService.rejectHireRequest(id, authentication.getName());
            logger.debug("Hire request rejected: {}", updatedRequest);
            return ResponseEntity.ok(updatedRequest);
        } catch (RuntimeException e) {
            logger.error("Failed to reject hire request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<HireRequest> completeHireRequest(
            @PathVariable String id,
            Authentication authentication) {
        logger.debug("Received request to complete hire request {} by user: {}", id, authentication.getName());
        try {
            HireRequest updatedRequest = hireRequestService.completeHireRequest(id, authentication.getName());
            logger.debug("Hire request completed: {}", updatedRequest);
            return ResponseEntity.ok(updatedRequest);
        } catch (RuntimeException e) {
            logger.error("Failed to complete hire request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }

    @GetMapping("/gig-worker")
    public ResponseEntity<List<HireRequest>> getHireRequestsForGigWorker(Authentication authentication) {
        logger.debug("Received request to fetch hire requests for gig worker: {}", authentication.getName());
        try {
            List<HireRequest> requests = hireRequestService.getHireRequestsForGigWorker(authentication.getName());
            logger.debug("Returning {} hire requests for gig worker", requests.size());
            return ResponseEntity.ok(requests);
        } catch (RuntimeException e) {
            logger.error("Failed to fetch hire requests for gig worker: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }

    @GetMapping("/client")
    public ResponseEntity<List<HireRequest>> getHireRequestsForClient(Authentication authentication) {
        logger.debug("Received request to fetch hire requests for client: {}", authentication.getName());
        try {
            List<HireRequest> requests = hireRequestService.getHireRequestsForClient(authentication.getName());
            logger.debug("Returning {} hire requests for client", requests.size());
            return ResponseEntity.ok(requests);
        } catch (RuntimeException e) {
            logger.error("Failed to fetch hire requests for client: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }

    @PostMapping("/{id}/update-status")
    public ResponseEntity<HireRequest> updateWorkStatus(
            @PathVariable String id,
            @RequestBody WorkStatusRequest workStatusRequest,
            Authentication authentication) {
        logger.debug("Received request to update work status for hire request {} by user: {}", id, authentication.getName());
        try {
            String workStatus = workStatusRequest.getWorkStatus();
            if (workStatus == null || (!workStatus.equals("IN_PROGRESS") && !workStatus.equals("COMPLETED"))) {
                throw new RuntimeException("Work status must be IN_PROGRESS or COMPLETED");
            }
            HireRequest updatedRequest = hireRequestService.updateWorkStatus(id, workStatus, authentication.getName());
            logger.debug("Work status updated: {}", updatedRequest);
            return ResponseEntity.ok(updatedRequest);
        } catch (RuntimeException e) {
            logger.error("Failed to update work status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping("/accepted")
    public ResponseEntity<List<HireRequest>> getAcceptedHireRequests(Authentication authentication) {
        logger.debug("Received request to fetch accepted hire requests for user: {}", authentication.getName());
        try {
            List<HireRequest> requests = hireRequestService.getAcceptedHireRequests(authentication.getName());
            logger.debug("Returning {} accepted hire requests", requests.size());
            return ResponseEntity.ok(requests);
        } catch (RuntimeException e) {
            logger.error("Failed to fetch accepted hire requests: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }

    @GetMapping("/service/{serviceId}")
    public ResponseEntity<List<HireRequest>> getHireRequestsByService(@PathVariable String serviceId, Authentication authentication) {
        logger.debug("Fetching hire requests for service: {}", serviceId);
        try {
            List<HireRequest> requests = hireRequestService.getHireRequestsByService(serviceId, authentication.getName());
            logger.debug("Returning {} hire requests for service", requests.size());
            return ResponseEntity.ok(requests);
        } catch (RuntimeException e) {
            logger.error("Failed to fetch hire requests for service: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }

    // HireRequestController.java (add at the bottom)
    static class WorkStatusRequest {
        private String workStatus;

        public String getWorkStatus() {
            return workStatus;
        }

        public void setWorkStatus(String workStatus) {
            this.workStatus = workStatus;
        }
    }
}