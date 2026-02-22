// File: src/main/java/com/example/workerlocator/repository/RatingRepository.java
// Rating repository.
package com.example.workerlocator.repository;

import com.example.workerlocator.model.Rating;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface RatingRepository extends MongoRepository<Rating, String> {
    List<Rating> findByWorkerId(String workerId);
}