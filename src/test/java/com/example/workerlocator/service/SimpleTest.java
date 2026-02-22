package com.example.workerlocator.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class SimpleTest {
    
    @Test
    void simpleTestCase() {
        assertTrue(true, "This test should always pass");
    }
    
    @Test
    void basicMathTest() {
        assertEquals(4, 2 + 2, "Basic addition should work");
    }
}