package com.example.gigconnect.controller;

import com.example.gigconnect.model.User;
import com.example.gigconnect.service.ReviewService;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
@GetMapping("/service/{serviceId}")
public ResponseEntity<List<User.Review>> getReviewsByService(@PathVariable String serviceId, Authentication authentication) {
    logger.debug("Fetching reviews for service: {}", serviceId);
    try {
        List<User.Review> reviews = reviewService.getReviewsByService(serviceId, authentication.getName());
        logger.debug("Returning {} reviews for service", reviews.size());
        return ResponseEntity.ok(reviews);
    } catch (RuntimeException e) {
        logger.error("Failed to fetch reviews for service: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
    }
}

    @PostMapping("/{gigWorkerId}")
    public ResponseEntity<User> addReview(
            @PathVariable String gigWorkerId,
            @RequestBody ReviewRequest reviewRequest,
            Authentication authentication) {
        logger.debug("Received request to add review for gig worker ID: {} by user: {}", gigWorkerId, authentication.getName());
        try {
            if (reviewRequest.getRating() < 1 || reviewRequest.getRating() > 5) {
                throw new RuntimeException("Rating must be between 1 and 5");
            }
            if (reviewRequest.getComment() == null || reviewRequest.getComment().trim().isEmpty()) {
                throw new RuntimeException("Comment is required");
            }
            // Validate that the hire request is completed (assumes ReviewService checks this)
            User updatedGigWorker = reviewService.addReview(
                    gigWorkerId,
                    authentication.getName(),
                    reviewRequest.getComment(),
                    reviewRequest.getRating(),
                    reviewRequest.getServiceId()
            );
            logger.debug("Review added for gig worker ID: {}", gigWorkerId);
            return ResponseEntity.ok(updatedGigWorker);
        } catch (RuntimeException e) {
            logger.error("Failed to add review: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    static class ReviewRequest {
        private String comment;
        private int rating;
        private String serviceId;

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