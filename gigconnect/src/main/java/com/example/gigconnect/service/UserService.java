package com.example.gigconnect.service;

import com.example.gigconnect.config.JwtUtil;
import com.example.gigconnect.dto.PublicUserProfileDTO;
import com.example.gigconnect.dto.UserProfileUpdateDTO;
import com.example.gigconnect.model.GigService;
import com.example.gigconnect.model.User;
import com.example.gigconnect.repository.GigServiceRepository;
import com.example.gigconnect.repository.UserRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Validated
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GigServiceRepository gigServiceRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager; 

    @Autowired
private RestTemplate restTemplate; // <-- ADD THIS
private final String vectorizerUrl = "http://localhost:5001/vectorize";

    public User registerUser(@Valid User user) {
        logger.debug("Registering user with email: {}", user.getEmail());
        if (userRepository.findByEmail(user.getEmail()) != null) {
            logger.error("Email already exists: {}", user.getEmail());
            throw new RuntimeException("Email already existed !!");
        }
        if (!user.getRole().equals("GIG_WORKER") && !user.getRole().equals("CLIENT") && 
            !user.getRole().equals("ADMIN")) {
            logger.error("Invalid role: {}", user.getRole());
            throw new RuntimeException("Not valid role");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        logger.debug("User registered: {}", savedUser);
        return savedUser;
    }

    public String loginUser(String email, String password) {
        logger.debug("Attempting login for email: {}", email);
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            String role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
            String token = jwtUtil.generateToken(email, role);
            logger.debug("Login successful for email: {}, generated token: {}", email, token);
            return token;
        } catch (AuthenticationException e) {
            logger.error("Invalid credentials for email: {}", email);
            throw new RuntimeException("Invalid credentials");
        }
    }

    public User updateProfile(String email, @Valid UserProfileUpdateDTO updatedUser) {
        logger.debug("Updating profile for email: {}", email);
        User existingUser = userRepository.findByEmail(email);
        if (existingUser == null) {
            logger.error("User not found with email: {}", email);
            throw new RuntimeException("User not found");
        } 
        if (updatedUser.getOpenToWork() != null) existingUser.setOpenToWork(updatedUser.getOpenToWork());
        if (updatedUser.getName() != null) existingUser.setName(updatedUser.getName());
        if (updatedUser.getCity() != null) existingUser.setCity(updatedUser.getCity());
        if (updatedUser.getState() != null) existingUser.setState(updatedUser.getState());
        if (updatedUser.getLocation() != null) existingUser.setLocation(updatedUser.getLocation());
        if (updatedUser.getSkills() != null && existingUser.getRole().equals("GIG_WORKER")) {
            existingUser.setSkills(updatedUser.getSkills());
        }
        if (updatedUser.getPortfolio() != null && existingUser.getRole().equals("GIG_WORKER")) {
            existingUser.setPortfolio(updatedUser.getPortfolio());
        }
        if (updatedUser.getMediaUrls() != null && existingUser.getRole().equals("GIG_WORKER")) {
            existingUser.setMediaUrls(updatedUser.getMediaUrls());
        }
        User updated = userRepository.save(existingUser);
        logger.debug("Profile updated for email: {}", email);
        return updated;
    }

  public PublicUserProfileDTO getPublicProfile(String userId) {
    logger.debug("Fetching public profile for userId: {}", userId);
    User user = userRepository.findById(userId)
            .orElseThrow(() -> {
                logger.error("User not found with id: {}", userId);
                return new RuntimeException("User not found");
            });
    if (!user.getRole().equals("GIG_WORKER")) {
        logger.warn("Profile requested for non-GIG_WORKER user: {}", userId);
        return null; // Or throw a custom exception if you want to restrict non-gig workers
    }
    PublicUserProfileDTO profile = new PublicUserProfileDTO();
    profile.setId(user.getId());
    profile.setOpenToWork(user.isOpenToWork());
    profile.setName(user.getName());
    profile.setCity(user.getCity());
    profile.setState(user.getState());
    profile.setSkills(user.getSkills());
    profile.setPortfolio(user.getPortfolio());
    profile.setMediaUrls(user.getMediaUrls());

    List<GigService> services = gigServiceRepository.findByUserId(userId);
    profile.setServices(services);

    List<User.Review> reviews = user.getReviews();
    if (reviews != null && !reviews.isEmpty()) {
        double averageRating = reviews.stream()
                .mapToInt(User.Review::getRating)
                .average()
                .orElse(0.0);
        profile.setAverageRating(averageRating);
        profile.setReviews(reviews.stream().map(review -> {
            PublicUserProfileDTO.ReviewDTO dto = new PublicUserProfileDTO.ReviewDTO();
            dto.setComment(review.getComment());
            dto.setRating(review.getRating());
            dto.setClientId(review.getClientId());
            dto.setClientName(review.getClientName());
            return dto;
        }).collect(Collectors.toList()));
    } else {
        profile.setAverageRating(0.0);
        profile.setReviews(null);
    }

    logger.debug("Public profile retrieved for userId: {}", userId);
    return profile;
}
    // UserService.java
public User getUserByEmail(String email) {
    logger.debug("Fetching user by email: {}", email);
    User user = userRepository.findByEmail(email);
    if (user == null) {
        logger.error("User not found with email: {}", email);
        throw new RuntimeException("User not found");
    }
    return user;
} 
public List<PublicUserProfileDTO> searchGigWorkers(String keyword, String city, String state, List<String> skills) {
    logger.debug("Searching gig workers with keyword: {}, city: {}, state: {}, skills: {}", keyword, city, state,
            skills);

    List<User> gigWorkers = gigServiceRepository.searchGigWorkers(keyword, city, state, skills);

    // Get unique gig worker IDs from the results
    Set<String> matchingGigWorkerIds = gigWorkers.stream()
            .map(User::getId)
            .collect(Collectors.toSet());

    List<PublicUserProfileDTO> profiles = matchingGigWorkerIds.stream()
            .map(this::getPublicProfile)
            .collect(Collectors.toList());

    logger.debug("Found {} gig workers matching search criteria", profiles.size());
    return profiles;
} 

// --- ADD THIS ENTIRE NEW METHOD ---
public List<PublicUserProfileDTO> searchGigWorkersSemantic(String keyword, String city, String state, List<String> skills) {
    logger.debug("Semantic searching for: {}", keyword);

    // 1. Get the query vector
    List<Double> queryVector = getVectorForText(keyword);
    if (queryVector == null) {
        logger.warn("Could not generate vector for keyword: {}", keyword);
        return new ArrayList<>(); // Return empty if vectorizing fails
    }

    // 2. Call the NEW repository method
    List<User> gigWorkers = gigServiceRepository.searchGigWorkersByVector(queryVector, city, state, skills);

    // 3. Your existing DTO conversion logic
    Set<String> matchingGigWorkerIds = gigWorkers.stream()
            .map(User::getId)
            .collect(Collectors.toSet());

    List<PublicUserProfileDTO> profiles = matchingGigWorkerIds.stream()
            .map(this::getPublicProfile)
            .collect(Collectors.toList());

    logger.debug("Found {} gig workers matching semantic search", profiles.size());
    return profiles;
}

// --- ADD THIS HELPER METHOD ---
private List<Double> getVectorForText(String text) {
    try {
        Map<String, String> requestBody = Map.of("text", text);
        Map<String, Object> response = restTemplate.postForObject(vectorizerUrl, requestBody, Map.class);
        if (response != null && response.containsKey("vector")) {
            return (List<Double>) response.get("vector");
        }
    } catch (Exception e) {
        logger.error("Failed to generate vector for text: " + text, e);
    }
    return null;
} 
public List<PublicUserProfileDTO> getRecommendedWorkers(String targetServiceId) {
        // 1. Find the service the client is looking at
        GigService targetService = gigServiceRepository.findById(targetServiceId)
                .orElseThrow(() -> new RuntimeException("Target service not found: " + targetServiceId));

        // 2. Get its vector
        List<Double> targetVector = targetService.getServiceVector();
        if (targetVector == null || targetVector.isEmpty()) {
            logger.warn("Target service {} has no vector. Cannot find recommendations.", targetServiceId);
            return new ArrayList<>(); // Can't recommend if we have no vector
        }

        // 3. Find semantically similar services
        List<GigService> similarServices = gigServiceRepository.findSimilarServices(targetVector, targetServiceId);
        
        // 4. Get the unique workers from these services
        Set<String> workerIdsToExclude = new HashSet<>();
        // Exclude the worker who owns the target service
        workerIdsToExclude.add(targetService.getUserId()); 

        List<PublicUserProfileDTO> recommendations = new ArrayList<>();
        
        for (GigService service : similarServices) {
            String workerId = service.getUserId();
            
            // If we haven't already added this worker, get their profile
            if (!workerIdsToExclude.contains(workerId)) {
                try {
                    // REUSE your existing getPublicProfile method!
                    PublicUserProfileDTO profile = getPublicProfile(workerId);
                    if (profile != null) {
                         recommendations.add(profile);
                    }
                    workerIdsToExclude.add(workerId); // Add to set so we don't add them again
                } catch (Exception e) {
                    logger.error("Error fetching public profile for recommended worker {}: {}", workerId, e.getMessage());
                }
            }
        }
        
        // 5. Sort the list: show highest-rated workers first
        recommendations.sort(Comparator.comparing(PublicUserProfileDTO::getAverageRating).reversed());

        return recommendations;
    }
}