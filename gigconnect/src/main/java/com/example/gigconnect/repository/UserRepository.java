package com.example.gigconnect.repository;

import com.example.gigconnect.model.User;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface UserRepository extends MongoRepository<User, String> {
    User findByEmail(String email); 

   @Query("{'role': 'GIG_WORKER', 'city': { $regex: ?0, $options: 'i' }, 'state': { $regex: ?1, $options: 'i' } }")
    List<User> findGigWorkersByLocation(String city, String state);
}