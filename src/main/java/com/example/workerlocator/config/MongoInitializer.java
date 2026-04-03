package com.example.workerlocator.config;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.geo.Point;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.workerlocator.model.User;
import com.example.workerlocator.repository.UserRepository;

@Component
public class MongoInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final Logger logger = LoggerFactory.getLogger(MongoInitializer.class);
    private final Random random = new Random();

    private User createTestWorker(UserRepository repo, PasswordEncoder encoder,
                                String email, String type, double charge,
                                double longitude, double latitude) {
        User worker = new User();
        worker.setEmail(email);
        worker.setPassword(encoder.encode("password123"));
        worker.setRole("worker");
        worker.setWorkerType(type);
        worker.setCharge(charge);
        worker.setAvailable(true);
        worker.setAverageRating(3.5 + random.nextDouble() * 1.5); // Random rating between 3.5 and 5.0
        worker.setRatingCount(5 + random.nextInt(16)); // Random number of ratings between 5 and 20
        worker.setLocation(new Point(longitude, latitude));
        worker.setCurrentJobId(null);
        worker = repo.save(worker);
        System.out.println("Created " + type + " with ID: " + worker.getId());
        return worker;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("MongoInitializer: Checking for existing users...");
        long userCount = 0;
        try {
            userCount = userRepository.count();
            System.out.println("MongoInitializer: Found " + userCount + " existing users");
        } catch (Exception e) {
            logger.warn("MongoInitializer: Cannot access MongoDB at startup — skipping test data initialization. Cause: {}", e.toString());
            return;
        }

        // Only add test users if none exist
        if (userCount == 0) {
            System.out.println("MongoInitializer: Creating test users...");
            
            try {
                // Create a test customer
                User customer = new User();
                customer.setEmail("customer@example.com");
                customer.setPassword(passwordEncoder.encode("password123"));
                customer.setRole("customer");
                customer.setLocation(new org.springframework.data.geo.Point(77.5946, 12.9716)); // Bangalore coordinates
                customer.setAdditionalLocations(java.util.Collections.emptyList());
                customer = userRepository.save(customer);
                System.out.println("MongoInitializer: Created customer user with ID: " + customer.getId());

                // Create multiple test workers with different specialties
                // Chennai area workers (around 13.0827° N, 80.2707° E)
                createTestWorker(userRepository, passwordEncoder, "muthu.plumber@gmail.com", "plumber", 500.0, 80.2707, 13.0827);
                createTestWorker(userRepository, passwordEncoder, "raja.plumber@gmail.com", "plumber", 450.0, 80.2657, 13.0877);
                createTestWorker(userRepository, passwordEncoder, "kumar.electrician@gmail.com", "electrician", 600.0, 80.2757, 13.0877);
                createTestWorker(userRepository, passwordEncoder, "senthil.electrician@gmail.com", "electrician", 550.0, 80.2657, 13.0777);
                createTestWorker(userRepository, passwordEncoder, "anbu.carpenter@gmail.com", "carpenter", 800.0, 80.2807, 13.0927);
                createTestWorker(userRepository, passwordEncoder, "ravi.painter@gmail.com", "painter", 700.0, 80.2557, 13.0727);
                createTestWorker(userRepository, passwordEncoder, "karthi.cleaner@gmail.com", "cleaner", 300.0, 80.2857, 13.0927);
                createTestWorker(userRepository, passwordEncoder, "tamil.gardener@gmail.com", "gardener", 400.0, 80.2607, 13.0677);
                
                System.out.println("MongoInitializer: Created all test workers successfully");
            } catch (Exception e) {
                logger.error("Error creating test users: {}", e.getMessage(), e);
            }
        }
    }
}