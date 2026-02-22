// File: src/main/java/com/example/workerlocator/controller/CustomerController.java
// Controller for customer actions.
package com.example.workerlocator.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.workerlocator.model.Rating;
import com.example.workerlocator.model.ServiceRequest;
import com.example.workerlocator.model.User;
import com.example.workerlocator.service.RatingService;
import com.example.workerlocator.service.RequestService;
import com.example.workerlocator.service.UserService;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    @PostMapping("/add-sample-workers")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> addSampleWorkers() {
        try {
            userService.addSampleWorkers();
            return ResponseEntity.ok("Sample workers added successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error adding sample workers: " + e.getMessage());
        }
    }

    @Autowired
    private UserService userService;

    @Autowired
    private RequestService requestService;

    @Autowired
    private RatingService ratingService;

    @GetMapping("/test/list-all-workers")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> listAllWorkers() {
        try {
            List<User> workers = userService.findAllWorkers();
            return ResponseEntity.ok(workers);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to list workers", "message", e.getMessage()));
        }
    }

    @GetMapping("/test/list-all-users")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> listAllUsers() {
        try {
            List<User> users = userService.findAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to list users", "message", e.getMessage()));
        }
    }

    @GetMapping("/nearby-workers")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getNearbyWorkers(@RequestParam double lat, @RequestParam double lng,
                                       @RequestParam(defaultValue = "10000") double maxDistance,
                                       @RequestParam(required = false) String workerType,
                                       @RequestParam(defaultValue = "0") double maxCharge) {
        try {
            System.out.println("Received request for nearby workers:");
            System.out.println("lat: " + lat + ", lng: " + lng);
            System.out.println("maxDistance: " + maxDistance);
            System.out.println("workerType: " + workerType);
            System.out.println("maxCharge: " + maxCharge);
            
            Point location = new Point(lng, lat); // Note: Point is (x=long, y=lat)
            List<User> workers = userService.findNearbyWorkers(location, maxDistance, workerType, maxCharge);
            System.out.println("Found " + workers.size() + " workers");
            workers.forEach(w -> System.out.println("Worker: " + w.getEmail() + ", Location: " + w.getLocation()));
            return ResponseEntity.ok(workers);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of(
                    "error", "Failed to find nearby workers",
                    "message", e.getMessage()
                ));
        }
    }

    @PostMapping("/add-location")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<String> addLocation(@RequestBody Map<String, Double> locData, @RequestParam String userId) {
        Point location = new Point(locData.get("lng"), locData.get("lat"));
        userService.addCustomerLocation(userId, location);
        return ResponseEntity.ok("Location added");
    }

    @PostMapping("/request-service")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ServiceRequest requestService(@RequestBody ServiceRequest request) {
        return requestService.createRequest(request);
    }

    @GetMapping("/requests")
    @PreAuthorize("hasRole('CUSTOMER')")
    public List<ServiceRequest> getRequests(@RequestParam String userId) {
        return requestService.getCustomerRequests(userId);
    }

    @PostMapping("/complete-request")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, String>> completeRequest(@RequestParam String requestId, @RequestParam String userId) {
        try {
            requestService.completeRequest(requestId, userId);
            return ResponseEntity.ok(Map.of("message", "Request completed successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal error: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Unexpected error occurred"));
        }
    }

    @PostMapping("/rate-worker")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Rating rateWorker(@RequestBody Rating rating) {
        return ratingService.addRating(rating);
    }
}