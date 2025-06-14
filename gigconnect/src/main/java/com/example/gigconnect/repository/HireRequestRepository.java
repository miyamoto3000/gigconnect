package com.example.gigconnect.repository;

import com.example.gigconnect.model.HireRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface HireRequestRepository extends MongoRepository<HireRequest, String> {
    List<HireRequest> findByGigWorkerId(String gigWorkerId);
    List<HireRequest> findByClientId(String clientId);
    List<HireRequest> findByGigWorkerIdAndStatus(String gigWorkerId, String status);
}