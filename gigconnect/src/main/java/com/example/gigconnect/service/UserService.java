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

import java.util.List;
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
            logger.error("User is not a GIG_WORKER: {}", userId);
            throw new RuntimeException("Only GIG_WORKER profiles are publicly viewable");
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

        // Fetch services
        List<GigService> services = gigServiceRepository.findByUserId(userId);
        profile.setServices(services);

        // Calculate average rating and set reviews
        List<User.Review> reviews = user.getReviews();
        if (reviews != null && !reviews.isEmpty()) {
            double averageRating = reviews.stream()
                    .mapToInt(User.Review::getRating)
                    .average()
                    .orElse(0.0);
            profile.setAverageRating(averageRating);

            List<PublicUserProfileDTO.ReviewDTO> reviewDTOs = reviews.stream().map(review -> {
                PublicUserProfileDTO.ReviewDTO dto = new PublicUserProfileDTO.ReviewDTO();
                dto.setComment(review.getComment());
                dto.setRating(review.getRating());
                dto.setClientId(review.getClientId());
                dto.setClientName(review.getClientName());
                return dto;
            }).collect(Collectors.toList());
            profile.setReviews(reviewDTOs);
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
// UserService.java
public List<PublicUserProfileDTO> searchGigWorkers(String keyword, String city, String state) {
    logger.debug("Searching gig workers with keyword: {}, city: {}, state: {}", keyword, city, state);
    
    // First, find users matching city and state (if provided)
    List<User> gigWorkers = userRepository.findAll().stream()
            .filter(user -> user.getRole().equals("GIG_WORKER"))
            .filter(user -> city == null || user.getCity() != null && user.getCity().equalsIgnoreCase(city))
            .filter(user -> state == null || user.getState() != null && user.getState().equalsIgnoreCase(state))
            .collect(Collectors.toList());
    
    List<String> gigWorkerIds = gigWorkers.stream().map(User::getId).collect(Collectors.toList());
    
    // Find services matching the keyword for these gig workers
    List<GigService> matchingServices = gigServiceRepository.findByUserIdsAndTitleOrCategory(gigWorkerIds, keyword);
    
    // Get unique gig worker IDs from matching services
    Set<String> matchingGigWorkerIds = matchingServices.stream()
            .map(GigService::getUserId)
            .collect(Collectors.toSet());
    
    // Convert to PublicUserProfileDTO
    List<PublicUserProfileDTO> profiles = matchingGigWorkerIds.stream()
            .map(this::getPublicProfile)
            .collect(Collectors.toList());
    
    logger.debug("Found {} gig workers matching search criteria", profiles.size());
    return profiles;
}
}