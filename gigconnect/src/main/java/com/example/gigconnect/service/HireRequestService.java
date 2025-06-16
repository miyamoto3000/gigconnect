package com.example.gigconnect.service;

import com.example.gigconnect.model.HireRequest;
import com.example.gigconnect.model.GigService;
import com.example.gigconnect.model.User;
import com.example.gigconnect.repository.HireRequestRepository;
import com.example.gigconnect.repository.GigServiceRepository;
import com.example.gigconnect.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HireRequestService {

    private static final Logger logger = LoggerFactory.getLogger(HireRequestService.class);

    @Autowired
    private HireRequestRepository hireRequestRepository;

    @Autowired
    private GigServiceRepository gigServiceRepository;

    @Autowired
    private UserRepository userRepository;

    public HireRequest createHireRequest(HireRequest hireRequest, String clientEmail) {
        logger.debug("Creating hire request for client email: {}", clientEmail);
        User client = userRepository.findByEmail(clientEmail);
        if (client == null || !client.getRole().equals("CLIENT")) {
            logger.error("User not found or not a CLIENT: {}", clientEmail);
            throw new RuntimeException("Only CLIENTs can send hire requests");
        }

        GigService service = gigServiceRepository.findById(hireRequest.getServiceId())
                .orElseThrow(() -> {
                    logger.error("Service not found: {}", hireRequest.getServiceId());
                    return new RuntimeException("Service not found");
                });

        User gigWorker = userRepository.findById(service.getUserId())
                .orElseThrow(() -> {
                    logger.error("Gig Worker not found: {}", service.getUserId());
                    return new RuntimeException("Gig Worker not found");
                });

        if (!gigWorker.getRole().equals("GIG_WORKER")) {
            logger.error("Target user is not a GIG_WORKER: {}", gigWorker.getId());
            throw new RuntimeException("Target user is not a GIG_WORKER");
        }

        // Validate requestedDateTime
        if (hireRequest.getRequestedDateTime() == null || hireRequest.getRequestedDateTime().isEmpty()) {
            logger.error("Requested date and time are required");
            throw new RuntimeException("Requested date and time are required");
        }

        try {
            LocalDateTime requestedDateTime = LocalDateTime.parse(hireRequest.getRequestedDateTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            LocalDateTime now = LocalDateTime.now();
            if (requestedDateTime.isBefore(now)) {
                logger.error("Requested date and time must be in the future: {}", hireRequest.getRequestedDateTime());
                throw new RuntimeException("Requested date and time must be in the future");
            }
        } catch (DateTimeParseException e) {
            logger.error("Invalid date format for requestedDateTime: {}", hireRequest.getRequestedDateTime());
            throw new RuntimeException("Invalid date format. Use ISO 8601 format (e.g., 2025-06-20T15:00:00)");
        }

        // Validate budget
        if (hireRequest.getBudget() <= 0) {
            logger.error("Budget must be a positive value: {}", hireRequest.getBudget());
            throw new RuntimeException("Budget must be a positive value");
        }

        hireRequest.setClientId(client.getId());
        hireRequest.setGigWorkerId(gigWorker.getId());
        hireRequest.setStatus("PENDING");
        hireRequest.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        HireRequest savedRequest = hireRequestRepository.save(hireRequest);
        logger.debug("Hire request created: {}", savedRequest);
        return savedRequest;
    }

    public HireRequest acceptHireRequest(String hireRequestId, String gigWorkerEmail) {
        logger.debug("Accepting hire request {} by gig worker email: {}", hireRequestId, gigWorkerEmail);
        HireRequest hireRequest = hireRequestRepository.findById(hireRequestId)
                .orElseThrow(() -> {
                    logger.error("Hire request not found: {}", hireRequestId);
                    return new RuntimeException("Hire request not found");
                });

        User gigWorker = userRepository.findByEmail(gigWorkerEmail);
        if (gigWorker == null || !gigWorker.getRole().equals("GIG_WORKER")) {
            logger.error("User not found or not a GIG_WORKER: {}", gigWorkerEmail);
            throw new RuntimeException("Only GIG_WORKERs can accept hire requests");
        }

        if (!hireRequest.getGigWorkerId().equals(gigWorker.getId())) {
            logger.error("Gig Worker not authorized to accept this hire request: {}", gigWorkerEmail);
            throw new RuntimeException("Not authorized to accept this hire request");
        }

        if (!hireRequest.getStatus().equals("PENDING")) {
            logger.error("Hire request is not in PENDING state: {}", hireRequest.getStatus());
            throw new RuntimeException("Hire request is not in PENDING state");
        }

        hireRequest.setStatus("ACCEPTED");
        hireRequest.setWorkStatus("IN_PROGRESS"); // Initialize workStatus
        HireRequest updatedRequest = hireRequestRepository.save(hireRequest);
        logger.debug("Hire request accepted: {}", updatedRequest);
        return updatedRequest;
    }

    public HireRequest rejectHireRequest(String hireRequestId, String gigWorkerEmail) {
        logger.debug("Rejecting hire request {} by gig worker email: {}", hireRequestId, gigWorkerEmail);
        HireRequest hireRequest = hireRequestRepository.findById(hireRequestId)
                .orElseThrow(() -> {
                    logger.error("Hire request not found: {}", hireRequestId);
                    return new RuntimeException("Hire request not found");
                });

        User gigWorker = userRepository.findByEmail(gigWorkerEmail);
        if (gigWorker == null || !gigWorker.getRole().equals("GIG_WORKER")) {
            logger.error("User not found or not a GIG_WORKER: {}", gigWorkerEmail);
            throw new RuntimeException("Only GIG_WORKERs can reject hire requests");
        }

        if (!hireRequest.getGigWorkerId().equals(gigWorker.getId())) {
            logger.error("Gig Worker not authorized to reject this hire request: {}", gigWorkerEmail);
            throw new RuntimeException("Not authorized to reject this hire request");
        }

        if (!hireRequest.getStatus().equals("PENDING")) {
            logger.error("Hire request is not in PENDING state: {}", hireRequest.getStatus());
            throw new RuntimeException("Hire request is not in PENDING state");
        }

        hireRequest.setStatus("REJECTED");
        HireRequest updatedRequest = hireRequestRepository.save(hireRequest);
        logger.debug("Hire request rejected: {}", updatedRequest);
        return updatedRequest;
    }

    public HireRequest updateWorkStatus(String hireRequestId, String workStatus, String gigWorkerEmail) {
        logger.debug("Updating work status for hire request {} by gig worker email: {}", hireRequestId, gigWorkerEmail);
        HireRequest hireRequest = hireRequestRepository.findById(hireRequestId)
                .orElseThrow(() -> {
                    logger.error("Hire request not found: {}", hireRequestId);
                    return new RuntimeException("Hire request not found");
                });

        User gigWorker = userRepository.findByEmail(gigWorkerEmail);
        if (gigWorker == null || !gigWorker.getRole().equals("GIG_WORKER")) {
            logger.error("User not found or not a GIG_WORKER: {}", gigWorkerEmail);
            throw new RuntimeException("Only GIG_WORKERs can update work status");
        }

        if (!hireRequest.getGigWorkerId().equals(gigWorker.getId())) {
            logger.error("Gig Worker not authorized to update this hire request: {}", gigWorkerEmail);
            throw new RuntimeException("Not authorized to update this hire request");
        }

        if (!hireRequest.getStatus().equals("ACCEPTED")) {
            logger.error("Hire request must be in ACCEPTED state to update work status: {}", hireRequest.getStatus());
            throw new RuntimeException("Hire request must be in ACCEPTED state to update work status");
        }

        if (!workStatus.equals("IN_PROGRESS") && !workStatus.equals("COMPLETED")) {
            logger.error("Invalid work status: {}. Must be IN_PROGRESS or COMPLETED", workStatus);
            throw new RuntimeException("Invalid work status. Must be IN_PROGRESS or COMPLETED");
        }

        hireRequest.setWorkStatus(workStatus);
        HireRequest updatedRequest = hireRequestRepository.save(hireRequest);
        logger.debug("Work status updated: {}", updatedRequest);
        return updatedRequest;
    }

    public HireRequest completeHireRequest(String hireRequestId, String gigWorkerEmail) {
        logger.debug("Completing hire request {} by gig worker email: {}", hireRequestId, gigWorkerEmail);
        HireRequest hireRequest = hireRequestRepository.findById(hireRequestId)
                .orElseThrow(() -> {
                    logger.error("Hire request not found: {}", hireRequestId);
                    return new RuntimeException("Hire request not found");
                });

        User gigWorker = userRepository.findByEmail(gigWorkerEmail);
        if (gigWorker == null || !gigWorker.getRole().equals("GIG_WORKER")) {
            logger.error("User not found or not a GIG_WORKER: {}", gigWorkerEmail);
            throw new RuntimeException("Only GIG_WORKERs can complete hire requests");
        }

        if (!hireRequest.getGigWorkerId().equals(gigWorker.getId())) {
            logger.error("Gig Worker not authorized to complete this hire request: {}", gigWorkerEmail);
            throw new RuntimeException("Not authorized to complete this hire request");
        }

        if (!hireRequest.getStatus().equals("ACCEPTED")) {
            logger.error("Hire request is not in ACCEPTED state: {}", hireRequest.getStatus());
            throw new RuntimeException("Hire request must be in ACCEPTED state to complete");
        }

        hireRequest.setWorkStatus("COMPLETED"); // Update workStatus instead of status
        HireRequest updatedRequest = hireRequestRepository.save(hireRequest);
        logger.debug("Hire request completed: {}", updatedRequest);
        return updatedRequest;
    }

    public List<HireRequest> getHireRequestsForGigWorker(String gigWorkerEmail) {
        logger.debug("Fetching hire requests for gig worker email: {}", gigWorkerEmail);
        User gigWorker = userRepository.findByEmail(gigWorkerEmail);
        if (gigWorker == null || !gigWorker.getRole().equals("GIG_WORKER")) {
            logger.error("User not found or not a GIG_WORKER: {}", gigWorkerEmail);
            throw new RuntimeException("Only GIG_WORKERs can view their hire requests");
        }
        List<HireRequest> requests = hireRequestRepository.findByGigWorkerId(gigWorker.getId());
        logger.debug("Found {} hire requests for gig worker: {}", requests.size(), gigWorkerEmail);
        return requests;
    }

    public List<HireRequest> getHireRequestsForClient(String clientEmail) {
        logger.debug("Fetching hire requests for client email: {}", clientEmail);
        User client = userRepository.findByEmail(clientEmail);
        if (client == null || !client.getRole().equals("CLIENT")) {
            logger.error("User not found or not a CLIENT: {}", clientEmail);
            throw new RuntimeException("Only CLIENTs can view their hire requests");
        }
        List<HireRequest> requests = hireRequestRepository.findByClientId(client.getId());
        logger.debug("Found {} hire requests for client: {}", requests.size(), clientEmail);
        return requests;
    }

    // HireRequestService.java
public List<HireRequest> getAcceptedHireRequests(String email) {
    logger.debug("Fetching accepted hire requests for user email: {}", email);
    User user = userRepository.findByEmail(email);
    if (user == null) {
        throw new RuntimeException("User not found");
    }
    if (user.getRole().equals("GIG_WORKER")) {
        return hireRequestRepository.findByGigWorkerIdAndStatus(user.getId(), "ACCEPTED");
    } else if (user.getRole().equals("CLIENT")) {
        return hireRequestRepository.findByClientId(user.getId()).stream()
                .filter(req -> req.getStatus().equals("ACCEPTED"))
                .collect(Collectors.toList());
    } else {
        throw new RuntimeException("Only CLIENTs and GIG_WORKERs can view accepted hire requests");
    }
}
    
}