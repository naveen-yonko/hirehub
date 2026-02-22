// File: src/main/java/com/example/workerlocator/service/UserService.java
// User service, implements UserDetailsService for security.
package com.example.workerlocator.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.workerlocator.model.User;
import com.example.workerlocator.repository.UserRepository;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MongoTemplate mongoTemplate;

    public User registerUser(User user) {
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new RuntimeException("Email already exists");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if ("worker".equals(user.getRole())) {
            user.setAvailable(true);
            user.setAverageRating(0.0);
            user.setRatingCount(0);
        } else {
            user.setAdditionalLocations(List.of());
        }
        return userRepository.save(user);
    }

    // Save an existing user without performing register checks (use for updates)
    public User saveUser(User user) {
        // Do not re-encode password here: assume password is already encoded for existing users
        return userRepository.save(user);
    }
    
    public void addSampleWorkers() {
        try {
            System.out.println("Adding sample workers...");
            
            // Clear existing sample workers first
            Query query = new Query(Criteria.where("email").regex("^(selvam|muthu|kannan|raja|kumar|pandi|murugan|rajan|vel|arun)@gmail\\.com$", "i"));
            mongoTemplate.remove(query, User.class);

            Object[][] workers = {
                // name, type, charge, lat offset (meters), lng offset (meters)
                {"Selvam", "electrician", 500.0, 0, 0},        // Center
                {"Muthu", "plumber", 400.0, 300, 300},        // NE nearby
                {"Kannan", "carpenter", 600.0, -200, -200},    // SW nearby
                {"Raja", "painter", 450.0, -150, 200},         // SE nearby
                {"Kumar", "mechanic", 550.0, 200, -150},       // NW nearby
                {"Pandi", "electrician", 450.0, 500, 0},       // E bit further
                {"Murugan", "plumber", 350.0, -400, 100},      // W bit further
                {"Rajan", "carpenter", 550.0, 100, 400},       // N bit further
                {"Vel", "painter", 400.0, -100, -450},         // S bit further
                {"Arun", "mechanic", 500.0, 600, 600}         // NE furthest
            };

            // Center point (Madurai central)
            double baseLat = 9.9252;
            double baseLng = 78.1198;

            // Earth's radius in meters
            double earthRadius = 6371000;

            for (Object[] w : workers) {
                String name = (String)w[0];
                String type = (String)w[1];
                double charge = (Double)w[2];
                double latOffset = (Integer)w[3];
                double lngOffset = (Integer)w[4];

                // Convert offset distances to coordinate offsets
                // δlat = distance/R (in radians)
                double latDiff = (latOffset / earthRadius) * (180.0 / Math.PI);
                // δlong = distance/(R × cos(lat)) (in radians)
                double lngDiff = (lngOffset / (earthRadius * Math.cos(Math.toRadians(baseLat)))) * (180.0 / Math.PI);

                double lat = baseLat + latDiff;
                double lng = baseLng + lngDiff;

                String email = name.toLowerCase() + "@gmail.com";
                
                // Skip if worker already exists
                if (userRepository.findByEmail(email) != null) {
                    System.out.println("Worker already exists: " + email);
                    continue;
                }

                User worker = new User();
                worker.setEmail(email);
                worker.setPassword(passwordEncoder.encode("password123"));
                worker.setRole("worker");
                worker.setLocation(new Point(lng, lat)); // MongoDB uses [longitude, latitude] order
                worker.setWorkerType(type.toLowerCase().trim());
                worker.setCharge(charge);
                worker.setAvailable(true);
                worker.setAverageRating(4.0 + Math.random());
                worker.setRatingCount((int)(8 + Math.random() * 12));

                double distanceInMeters = calculateDistance(new Point(baseLng, baseLat), worker.getLocation());
                System.out.println(String.format(
                    "Adding worker: %s\n  Type: %s\n  Location: [%.6f, %.6f]\n  Distance from center: %.0f meters\n  Charge: ₹%.2f/hour",
                    name, type, lng, lat, distanceInMeters, charge
                ));

                userRepository.save(worker);
                System.out.println(String.format(
                    "Added worker: %s\n  Type: %s\n  Location: [%.6f, %.6f]\n  Charge: ₹%.2f/hour",
                    name, type, lng, lat, charge
                ));
            }
            System.out.println("Sample workers added successfully.");
        } catch (Exception e) {
            System.err.println("Error adding sample workers: " + e.getMessage());
            System.err.println("Stack trace: " + e);
        }
    }

    public User loginUser(String email, String password, String role) {
        User user = userRepository.findByEmail(email);
        if (user == null || !user.getRole().equals(role)) {
            throw new RuntimeException("Invalid credentials");
        }

        // First try normal BCrypt match
        boolean matches = passwordEncoder.matches(password, user.getPassword());

        // Dev-friendly fallback: if legacy user has plain-text stored password, accept once and upgrade to BCrypt
        if (!matches && user.getPassword() != null && user.getPassword().equals(password)) {
            user.setPassword(passwordEncoder.encode(password));
            userRepository.save(user);
            matches = true;
        }

        if (!matches) {
            throw new RuntimeException("Invalid credentials");
        }
        return user;
    }

    public List<User> findNearbyWorkers(Point location, double maxDistance, String workerType, double maxCharge) {
        try {
            System.out.println("\nSearching for workers with criteria:");
            System.out.println(String.format("- Location: [%.6f, %.6f]", location.getX(), location.getY()));
            System.out.println(String.format("- Max Distance: %.0f meters", maxDistance));
            System.out.println("- Worker Type: " + (workerType != null && !workerType.trim().isEmpty() ? workerType : "any"));
            System.out.println("- Max Charge: " + (maxCharge > 0 ? "₹" + maxCharge + "/hour" : "any"));

            // maxDistance expected in meters. MongoDB nearSphere expects distance in radians when using spherical queries
            double maxDistanceInRadians = maxDistance / 6371000.0;
            
            System.out.println("Search params - Location: " + location + ", Distance: " + maxDistance + 
                              ", WorkerType: " + workerType + ", MaxCharge: " + maxCharge);

            // Verify MongoDB connection before proceeding
            try {
                mongoTemplate.getDb().runCommand(new org.bson.Document("ping", 1));
            } catch (Exception e) {
                System.err.println("MongoDB connection error: " + e.getMessage());
                throw new RuntimeException("Database connection error. Please try again later.", e);
            }

            // Build query using compound index
            Query query = new Query();
            
            // Add role and availability criteria
            query.addCriteria(Criteria.where("role").is("worker"));
            query.addCriteria(Criteria.where("available").is(true));
            
            // Add location criteria first to utilize the geospatial index
            if (!Double.isNaN(location.getX()) && !Double.isNaN(location.getY())) {
                query.addCriteria(Criteria.where("location").nearSphere(location).maxDistance(maxDistanceInRadians));
            }
            
            // Add worker type criteria to utilize the compound index
            if (workerType != null && !workerType.trim().isEmpty()) {
                // Use case-insensitive partial match instead of strict prefix match
                query.addCriteria(Criteria.where("workerType").regex(workerType.trim(), "i"));
            }
            
            // Add charge criteria
            if (maxCharge > 0) {
                query.addCriteria(Criteria.where("charge").lte(maxCharge));
            }
            
            System.out.println("MongoDB Query: " + query.toString());
            
            List<User> results = mongoTemplate.find(query, User.class);
            System.out.println("Found " + results.size() + " nearby workers");
            
            if (results.isEmpty()) {
                System.out.println("No workers found. Query criteria:");
                System.out.println("- Location: " + location);
                System.out.println("- Max Distance: " + maxDistance + "m (" + maxDistanceInRadians + " radians)");
                System.out.println("- Worker Type: " + (workerType != null ? workerType : "any"));
                System.out.println("- Max Charge: " + (maxCharge > 0 ? maxCharge : "any"));
            } else {
                // Debug output for results
                results.forEach(worker -> {
                    double distance = calculateDistance(location, worker.getLocation());
                    System.out.println(
                        String.format("Worker: %s\n  - Type: %s\n  - Location: %s\n  - Distance: %.2fm\n  - Charge: ₹%.2f/hour\n  - Rating: %.1f (%d reviews)",
                            worker.getEmail(),
                            worker.getWorkerType(),
                            worker.getLocation(),
                            distance,
                            worker.getCharge(),
                            worker.getAverageRating(),
                            worker.getRatingCount()
                        )
                    );
                });
            }
            return results;
            
        } catch (IllegalStateException | org.springframework.dao.DataAccessException e) {
            System.err.println("Error finding nearby workers: " + e.getMessage());
            throw new RuntimeException("Failed to find nearby workers: " + e.getMessage());
        }
    }

    public void updateAvailability(String userId, boolean available) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent() && "worker".equals(optionalUser.get().getRole())) {
            User user = optionalUser.get();
            user.setAvailable(available);
            userRepository.save(user);
        }
    }

    public void updateWorkerDetails(String userId, String workerType, double charge) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (!optionalUser.isPresent()) {
            throw new RuntimeException("Worker not found");
        }
        
        User user = optionalUser.get();
        if (!"worker".equals(user.getRole())) {
            throw new RuntimeException("User is not a worker");
        }
        
        if (workerType == null || workerType.trim().isEmpty()) {
            throw new RuntimeException("Worker type cannot be empty");
        }
        
        if (charge < 0) {
            throw new RuntimeException("Charge cannot be negative");
        }

        user.setWorkerType(workerType.trim());
        user.setCharge(charge);
        userRepository.save(user);
    }

    public void addCustomerLocation(String userId, Point location) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent() && "customer".equals(optionalUser.get().getRole())) {
            User user = optionalUser.get();
            user.getAdditionalLocations().add(location);
            userRepository.save(user);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().toUpperCase())
                .build();
    }

    public User getUserById(String id) {
        return userRepository.findById(id).orElse(null);
    }

    public void updateRating(String workerId, Double newScore) {
        User worker = getUserById(workerId);
        if (worker != null && "worker".equals(worker.getRole())) {
            double newAverage = ((worker.getAverageRating() * worker.getRatingCount()) + newScore) / (worker.getRatingCount() + 1);
            worker.setAverageRating(newAverage);
            worker.setRatingCount(worker.getRatingCount() + 1);
            userRepository.save(worker);
        }
    }

    public List<User> findAllWorkers() {
        Query query = new Query(Criteria.where("role").is("worker"));
        List<User> workers = mongoTemplate.find(query, User.class);
        System.out.println("Found " + workers.size() + " total workers in database");
        workers.forEach(w -> System.out.println("Worker: " + w.getEmail() + ", Type: " + w.getWorkerType() + ", Location: " + w.getLocation()));
        return workers;
    }

    public List<User> findAllUsers() {
        List<User> users = mongoTemplate.findAll(User.class);
        System.out.println("Found " + users.size() + " total users in database");
        users.forEach(u -> System.out.println("User: " + u.getEmail() + ", Role: " + u.getRole()));
        return users;
    }

    private double calculateDistance(Point p1, Point p2) {
        if (p1 == null || p2 == null) return Double.POSITIVE_INFINITY;
        
        double earthRadius = 6371000; // meters
        double dLat = Math.toRadians(p2.getY() - p1.getY());
        double dLng = Math.toRadians(p2.getX() - p1.getX());
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                   Math.cos(Math.toRadians(p1.getY())) * Math.cos(Math.toRadians(p2.getY())) *
                   Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return earthRadius * c;
    }
}