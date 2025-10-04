package com.example.gigconnect.repository;

import com.example.gigconnect.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.ArrayList;
import java.util.List;

public class GigServiceRepositoryCustomImpl implements GigServiceRepositoryCustom {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<User> searchGigWorkers(String keyword, String city, String state, List<String> skills) {
        List<AggregationOperation> operations = new ArrayList<>();

        // Match services by keyword
        operations.add(Aggregation.match(
                Criteria.where("title").regex(keyword, "i")
                        .orOperator(Criteria.where("category").regex(keyword, "i"))));

        // Join with the users collection
        LookupOperation lookupOperation = LookupOperation.newLookup()
                .from("users")
                .localField("userId")
                .foreignField("_id")
                .as("gigWorker");
        operations.add(lookupOperation);

        // Unwind the gigWorker array
        operations.add(Aggregation.unwind("gigWorker"));
        
        // Filter the joined users
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
        
        // Replace the root with the gigWorker document
        operations.add(Aggregation.replaceRoot("gigWorker"));

        Aggregation aggregation = Aggregation.newAggregation(operations);

        return mongoTemplate.aggregate(aggregation, "services", User.class).getMappedResults();
    }
}