// Create new file: src/main/java/com/example/gigconnect/service/PaymentService.java
package com.example.gigconnect.service;

import com.example.gigconnect.controller.PaymentVerificationRequest;
import com.example.gigconnect.model.*;
import com.example.gigconnect.repository.*;
import com.razorpay.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private HireRequestRepository hireRequestRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private NotificationService notificationService;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;
    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    public Map<String, String> createOrder(String hireRequestId, String clientEmail) {
        User client = userRepository.findByEmail(clientEmail);
        HireRequest hireRequest = hireRequestRepository.findById(hireRequestId)
                .orElseThrow(() -> new RuntimeException("Hire Request not found with ID: " + hireRequestId));

        if (!hireRequest.getClientId().equals(client.getId())) {
            throw new RuntimeException("User is not authorized to pay for this hire request.");
        }
        if (!"ACCEPTED".equals(hireRequest.getStatus()) || !"PENDING".equals(hireRequest.getPaymentStatus())) {
            throw new RuntimeException("Payment can only be initiated for an accepted and pending payment request.");
        }

        try {
            RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", hireRequest.getBudget() * 100);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", hireRequestId);

            Order order = razorpayClient.orders.create(orderRequest);
            String orderId = order.get("id");

            hireRequest.setRazorpayOrderId(orderId);
            hireRequestRepository.save(hireRequest);
            logger.info("Razorpay Order created for HireRequest ID {}: {}", hireRequestId, orderId);

            Map<String, String> response = new HashMap<>();
            response.put("orderId", orderId);
            response.put("keyId", razorpayKeyId);
            return response;
        } catch (RazorpayException e) {
            logger.error("Error creating Razorpay order: {}", e.getMessage());
            throw new RuntimeException("Failed to create Razorpay order.", e);
        }
    }

    public void verifyPaymentAndUpdateStatus(PaymentVerificationRequest request) {
        String orderId = request.getRazorpay_order_id();
        try {
            boolean isValidSignature = Utils.verifyPaymentSignature(request.getProperties(), this.razorpayKeySecret);

            if (isValidSignature) {
                logger.info("Payment signature verified for Order ID: {}", orderId);
                HireRequest hireRequest = hireRequestRepository.findByRazorpayOrderId(orderId)
                        .orElseThrow(() -> new RuntimeException("Hire Request not found with Razorpay Order ID: " + orderId));

                hireRequest.setPaymentStatus("PAID");
                hireRequest.setWorkStatus("IN_PROGRESS");
                hireRequestRepository.save(hireRequest);

                Payment payment = new Payment();
                payment.setHireRequestId(hireRequest.getId());
                payment.setRazorpayPaymentId(request.getRazorpay_payment_id());
                payment.setRazorpayOrderId(orderId);
                payment.setRazorpaySignature(request.getRazorpay_signature());
                payment.setStatus("success");
                payment.setAmount(hireRequest.getBudget());
                payment.setCreatedAt(LocalDateTime.now());
                paymentRepository.save(payment);

                User gigWorker = userRepository.findById(hireRequest.getGigWorkerId()).orElse(null);
                User client = userRepository.findById(hireRequest.getClientId()).orElse(null);
                if (gigWorker != null && client != null) {
                    String notificationContent = "Payment received from " + client.getName() + "! You can now begin work.";
                    notificationService.sendNotification(new Notification(notificationContent, gigWorker.getId()));
                }
            } else {
                logger.warn("Invalid payment signature for Order ID: {}", orderId);
                throw new RuntimeException("Invalid payment signature.");
            }
        } catch (Exception e) {
            logger.error("Error during payment verification: {}", e.getMessage());
            throw new RuntimeException("Payment verification failed.", e);
        }
    }
}