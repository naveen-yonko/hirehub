package com.example.workerlocator.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.workerlocator.model.Rating;
import com.example.workerlocator.repository.RatingRepository;

@ExtendWith(MockitoExtension.class)
public class RatingServiceTest {

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private RatingService ratingService;

    private Rating testRating;

    @BeforeEach
    public void setUp() {
        testRating = new Rating();
        testRating.setWorkerId("worker123");
        testRating.setCustomerId("customer456");
        testRating.setScore(4.5);
        testRating.setComment("Great service!");
    }

    @Test
    void whenAddRating_thenSaveRatingAndUpdateUserRating() {
        // Arrange
        when(ratingRepository.save(any(Rating.class))).thenReturn(testRating);
        doNothing().when(userService).updateRating(anyString(), anyDouble());

        // Act
        Rating savedRating = ratingService.addRating(testRating);

        // Assert
        assertNotNull(savedRating, "Saved rating should not be null");
        assertEquals(testRating.getWorkerId(), savedRating.getWorkerId(), "Worker ID should match");
        assertEquals(testRating.getCustomerId(), savedRating.getCustomerId(), "Customer ID should match");
        assertEquals(testRating.getScore(), savedRating.getScore(), "Score should match");
        assertEquals(testRating.getComment(), savedRating.getComment(), "Comment should match");

        // Verify interactions
        verify(ratingRepository, times(1)).save(any(Rating.class));
        verify(userService, times(1)).updateRating(testRating.getWorkerId(), testRating.getScore());
    }

    @Test
    void whenAddRating_withNullScore_thenThrowException() {
        // Arrange
        testRating.setScore(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ratingService.addRating(testRating);
        }, "Should throw IllegalArgumentException when score is null");

        assertEquals("Rating score cannot be null", exception.getMessage());

        // Verify no interactions occurred
        verify(ratingRepository, never()).save(any(Rating.class));
        verify(userService, never()).updateRating(anyString(), anyDouble());
    }

    @Test
    void whenAddRating_withInvalidScore_thenThrowException() {
        // Arrange
        testRating.setScore(6.0); // Score greater than 5

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ratingService.addRating(testRating);
        }, "Should throw IllegalArgumentException when score is invalid");

        assertEquals("Rating score must be between 0 and 5", exception.getMessage());

        // Verify no interactions occurred
        verify(ratingRepository, never()).save(any(Rating.class));
        verify(userService, never()).updateRating(anyString(), anyDouble());
    }
}