// Create new file: src/main/java/com/example/gigconnect/controller/PaymentController.java
package com.example.gigconnect.controller;

import com.example.gigconnect.service.PaymentService;
import lombok.Data;
import org.json.JSONObject;
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

// DTO Classes
@Data
class OrderRequest {
    private String hireRequestId;
}

@Data
class PaymentVerificationRequest {
    private String razorpay_payment_id;
    private String razorpay_order_id;
    private String razorpay_signature;
    
    public JSONObject getProperties() {
        JSONObject options = new JSONObject();
        options.put("razorpay_order_id", this.razorpay_order_id);
        options.put("razorpay_payment_id", this.razorpay_payment_id);
        options.put("razorpay_signature", this.razorpay_signature);
        return options;
    }
}

@Data
class ApiResponse {
    private String message;
    public ApiResponse(String message) { this.message = message; }
}