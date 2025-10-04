package com.example.gigconnect.repository;

import com.example.gigconnect.model.User;
import java.util.List;

public interface GigServiceRepositoryCustom {
    List<User> searchGigWorkers(String keyword, String city, String state, List<String> skills);
}