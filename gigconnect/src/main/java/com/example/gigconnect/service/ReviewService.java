package com.example.gigconnect.service;

import com.example.gigconnect.model.HireRequest;
import com.example.gigconnect.model.User;
import com.example.gigconnect.repository.HireRequestRepository;
import com.example.gigconnect.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);

    @Autowired
    private UserRepository userRepository;

@Autowired
private HireRequestRepository hireRequestRepository;

public User addReview(String gigWorkerId, String clientEmail, String comment, int rating, String serviceId) {
    logger.debug("Adding review for gig worker ID: {} by client email: {}", gigWorkerId, clientEmail);

    // Validate gig worker
    User gigWorker = userRepository.findById(gigWorkerId)
            .orElseThrow(() -> {
                logger.error("Gig Worker not found: {}", gigWorkerId);
                return new RuntimeException("Gig Worker not found");
            });

    if (!gigWorker.getRole().equals("GIG_WORKER")) {
        throw new RuntimeException("User is not a GIG_WORKER");
    }

    // Validate client
    User client = userRepository.findByEmail(clientEmail);
    if (client == null || !client.getRole().equals("CLIENT")) {
        throw new RuntimeException("Only CLIENTs can leave reviews");
    }

    // Validate hire request
    List<HireRequest> hireRequests = hireRequestRepository.findByClientId(client.getId());
    HireRequest hireRequest = hireRequests.stream()
            .filter(req -> req.getGigWorkerId().equals(gigWorkerId) &&
                          req.getServiceId().equals(serviceId) &&
                          req.getStatus().equals("ACCEPTED") &&
                          req.getWorkStatus() != null &&
                          req.getWorkStatus().equals("COMPLETED"))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No completed hire request found for this service"));

    // Validate rating
    if (rating < 1 || rating > 5) {
        throw new RuntimeException("Rating must be between 1 and 5");
    }

    // Add review to gig worker
    User.Review review = new User.Review();
    review.setClientId(client.getId());
    review.setClientName(client.getName());
    review.setComment(comment);
    review.setRating(rating);
    review.setServiceId(serviceId);

    List<User.Review> reviews = gigWorker.getReviews();
    if (reviews == null) {
        reviews = new ArrayList<>();
    }
    reviews.add(review);
    gigWorker.setReviews(reviews);

    User updatedGigWorker = userRepository.save(gigWorker);
    logger.debug("Review added for gig worker ID: {}", gigWorkerId);
    return updatedGigWorker;
}
}