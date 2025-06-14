package com.example.gigconnect.repository;

import com.example.gigconnect.model.GigService;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface GigServiceRepository extends MongoRepository<GigService, String> {
    List<GigService> findByUserId(String userId);

    @Query("{ $or: [ { 'title': { $regex: ?0, $options: 'i' } }, { 'category': { $regex: ?0, $options: 'i' } } ] }")
    List<GigService> findByTitleOrCategory(String keyword);

    @Query("{ 'userId': { $in: ?0 }, $or: [ { 'title': { $regex: ?1, $options: 'i' } }, { 'category': { $regex: ?1, $options: 'i' } } ] }")
    List<GigService> findByUserIdsAndTitleOrCategory(List<String> userIds, String keyword);
}