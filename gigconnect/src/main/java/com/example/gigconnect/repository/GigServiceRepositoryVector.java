package com.example.gigconnect.repository;
import com.example.gigconnect.model.GigService;
import com.example.gigconnect.model.User;
import java.util.List;

public interface GigServiceRepositoryVector {
    List<User> searchGigWorkersByVector(List<Double> queryVector, String city, String state, List<String> skills); 

    List<GigService> findSimilarServices(List<Double> queryVector, String excludeServiceId);
}