package com.example.gigconnect.controller;

import com.example.gigconnect.model.User;
import com.example.gigconnect.service.ReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private static final Logger logger = LoggerFactory.getLogger(ReviewController.class);

    @Autowired
    private ReviewService reviewService;

  // ReviewController.java
@PostMapping("/{gigWorkerId}")
public ResponseEntity<User> addReview(
        @PathVariable String gigWorkerId,
        @RequestBody ReviewRequest reviewRequest,
        Authentication authentication) {
    logger.debug("Received request to add review for gig worker ID: {} by user: {}", gigWorkerId, authentication.getName());
    User updatedGigWorker = reviewService.addReview(
            gigWorkerId,
            authentication.getName(),
            reviewRequest.getComment(),
            reviewRequest.getRating(),
            reviewRequest.getServiceId()
    );
    logger.debug("Review added for gig worker ID: {}", gigWorkerId);
    return ResponseEntity.ok(updatedGigWorker);
}

class ReviewRequest {
    private String comment;
    private int rating;
    private String serviceId; // New field

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }
}
}