package com.example.gigconnect.service;

import com.example.gigconnect.config.JwtUtil;
import com.example.gigconnect.dto.UserProfileUpdateDTO;
import com.example.gigconnect.model.User;
import com.example.gigconnect.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public User registerUser(@Valid User user) {
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new RuntimeException("Emial already existed !!");
        }
        if (!user.getRole().equals("GIG_WORKER") && !user.getRole().equals("CLIENT") && 
            !user.getRole().equals("ADMIN")) {
            throw new RuntimeException("Not valid role");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public String loginUser(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("invalid credentials");
        }
        return jwtUtil.generateToken(email, user.getRole());
    }

    public User updateProfile(String email, @Valid UserProfileUpdateDTO updatedUser) {
        User existingUser = userRepository.findByEmail(email);
        if (existingUser == null) {
            throw new RuntimeException("User not found");
        }
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
        return userRepository.save(existingUser);
    }
}