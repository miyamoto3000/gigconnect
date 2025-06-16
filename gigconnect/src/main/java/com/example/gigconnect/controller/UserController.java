package com.example.gigconnect.controller;

import com.example.gigconnect.dto.PublicUserProfileDTO;
import com.example.gigconnect.dto.UserProfileUpdateDTO;
import com.example.gigconnect.model.User;
import com.example.gigconnect.service.UserService;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*; 
import org.springframework.security.core.Authentication; // Correct import

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody User user) {
        logger.debug("Received request to register user: {}", user.getEmail());
        User registeredUser = userService.registerUser(user);
        logger.debug("User registered: {}", registeredUser.getEmail());
        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        logger.debug("Received login request for email: {}", request.getEmail());
        String token = userService.loginUser(request.getEmail(), request.getPassword());
        logger.debug("Login successful for email: {}", request.getEmail());
        return ResponseEntity.ok(token);
    }

    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(@Valid @RequestBody UserProfileUpdateDTO updatedUser) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.debug("Received request to update profile for user: {}", email);
        User updated = userService.updateProfile(email, updatedUser);
        logger.debug("Profile updated for user: {}", email);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}/profile")
    public ResponseEntity<PublicUserProfileDTO> getPublicProfile(@PathVariable String id) {
        logger.debug("Received request to fetch public profile for userId: {}", id);
        PublicUserProfileDTO profile = userService.getPublicProfile(id);
        logger.debug("Public profile retrieved for userId: {}", id);
        return ResponseEntity.ok(profile);
    } 
    // UserController.java
@GetMapping("/me")
public ResponseEntity<User> getCurrentUser(Authentication authentication) {
    logger.debug("Received request to fetch current user: {}", authentication.getName());
    User user = userService.getUserByEmail(authentication.getName());
    logger.debug("Current user retrieved: {}", user.getEmail());
    return ResponseEntity.ok(user);
}
}

class LoginRequest {
    private String email;
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}