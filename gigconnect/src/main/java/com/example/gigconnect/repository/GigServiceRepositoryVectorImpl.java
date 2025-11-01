package com.example.gigconnect.repository;

import com.example.gigconnect.model.User;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.ArrayList;
import java.util.List;

// Notice this is a new class, not modifying the old one
public class GigServiceRepositoryVectorImpl implements GigServiceRepositoryVector {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<User> searchGigWorkersByVector(List<Double> queryVector, String city, String state, List<String> skills) {

        List<AggregationOperation> operations = new ArrayList<>();

        // Step 1: Use $vectorSearch (the new part)
        Document vectorSearchStage = new Document("$vectorSearch",
            new Document("index", "default") // "default" is the name of the index
                .append("path", "serviceVector")
                .append("queryVector", queryVector)
                .append("numCandidates", 150) // How many records to check
                .append("limit", 20)         // How many to return
        );
        operations.add(context -> vectorSearchStage);

        // Step 2: Your existing, unmodified aggregation logic
        operations.add(Aggregation.lookup("users", "userId", "_id", "gigWorker"));
        operations.add(Aggregation.unwind("gigWorker"));

        List<Criteria> userCriteria = new ArrayList<>();
        userCriteria.add(Criteria.where("gigWorker.role").is("GIG_WORKER"));
        if (city != null && !city.isEmpty()) {
            userCriteria.add(Criteria.where("gigWorker.city").regex(city, "i"));
        }
        if (state != null && !state.isEmpty()) {
            userCriteria.add(Criteria.where("gigWorker.state").regex(state, "i"));
        }
        if (skills != null && !skills.isEmpty()) {
            userCriteria.add(Criteria.where("gigWorker.skills").in(skills));
        }
        operations.add(Aggregation.match(new Criteria().andOperator(userCriteria.toArray(new Criteria[0]))));

        operations.add(Aggregation.replaceRoot("gigWorker"));

        Aggregation aggregation = Aggregation.newAggregation(operations);
        return mongoTemplate.aggregate(aggregation, "services", User.class).getMappedResults();
    }
}