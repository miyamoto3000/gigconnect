package com.example.gigconnect.controller;

import com.example.gigconnect.dto.UserProfileUpdateDTO;
import com.example.gigconnect.model.User;
import com.example.gigconnect.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public User register(@Valid @RequestBody User user) {
        return userService.registerUser(user);
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {
        return userService.loginUser(request.getEmail(), request.getPassword());
    }

    @PutMapping("/profile")
    public User updateProfile(@Valid @RequestBody UserProfileUpdateDTO updatedUser) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.updateProfile(email, updatedUser);
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