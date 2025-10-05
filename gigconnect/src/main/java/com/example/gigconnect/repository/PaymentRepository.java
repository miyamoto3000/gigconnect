// Create new file: src/main/java/com/example/gigconnect/repository/PaymentRepository.java
package com.example.gigconnect.repository;

import com.example.gigconnect.model.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface PaymentRepository extends MongoRepository<Payment, String> {
    Optional<Payment> findByRazorpayPaymentId(String razorpayPaymentId);
}