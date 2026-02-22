// File: src/main/java/com/example/workerlocator/service/RatingService.java
// Service for ratings.
package com.example.workerlocator.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.workerlocator.model.Rating;
import com.example.workerlocator.repository.RatingRepository;

@Service
public class RatingService {

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private UserService userService;

    public Rating addRating(Rating rating) {
        validateRating(rating);
        Rating saved = ratingRepository.save(rating);
        userService.updateRating(rating.getWorkerId(), rating.getScore());
        return saved;
    }

    private void validateRating(Rating rating) {
        if (rating.getScore() == null) {
            throw new IllegalArgumentException("Rating score cannot be null");
        }
        if (rating.getScore() < 0 || rating.getScore() > 5) {
            throw new IllegalArgumentException("Rating score must be between 0 and 5");
        }
    }
}