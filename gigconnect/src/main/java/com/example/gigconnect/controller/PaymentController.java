// File: src/main/java/com/example/gigconnect/controller/PaymentController.java
package com.example.gigconnect.controller;

import com.example.gigconnect.dto.ApiResponse;
import com.example.gigconnect.dto.OrderRequest;
import com.example.gigconnect.dto.PaymentVerificationRequest;
import com.example.gigconnect.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/create-order")
    public ResponseEntity<Map<String, String>> createOrder(@RequestBody OrderRequest orderRequest, Authentication authentication) {
        String clientEmail = authentication.getName();
        Map<String, String> response = paymentService.createOrder(orderRequest.getHireRequestId(), clientEmail);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-payment")
    public ResponseEntity<ApiResponse> verifyPayment(@RequestBody PaymentVerificationRequest verificationRequest) {
        paymentService.verifyPaymentAndUpdateStatus(verificationRequest);
        return ResponseEntity.ok(new ApiResponse("Payment verified successfully."));
    }
}