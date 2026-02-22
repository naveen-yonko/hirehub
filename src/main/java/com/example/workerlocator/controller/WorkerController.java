// File: src/main/java/com/example/workerlocator/controller/WorkerController.java
// Controller for worker actions.
package com.example.workerlocator.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.workerlocator.model.ServiceRequest;
import com.example.workerlocator.model.User;
import com.example.workerlocator.service.RequestService;
import com.example.workerlocator.service.UserService;

@RestController
@RequestMapping("/api/worker")
@PreAuthorize("hasRole('WORKER')")
public class WorkerController {

    @Autowired
    private UserService userService;

    @Autowired
    private RequestService requestService;

    @GetMapping("/details")
    public ResponseEntity<User> getWorkerDetails(@RequestParam String userId) {
        User worker = userService.getUserById(userId);
        if (worker != null && "worker".equals(worker.getRole())) {
            return ResponseEntity.ok(worker);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/update-availability")
    public ResponseEntity<String> updateAvailability(@RequestParam String userId, @RequestParam boolean available) {
        userService.updateAvailability(userId, available);
        return ResponseEntity.ok("Availability updated");
    }

    @PostMapping("/update-details")
    public ResponseEntity<?> updateDetails(@RequestParam String userId, @RequestBody Map<String, Object> data) {
        try {
            String workerType = (String) data.get("workerType");
            
            // Handle different number types from JSON
            Object chargeObj = data.get("charge");
            double charge = switch (chargeObj) {
                case Integer i -> i.doubleValue();
                case Double d -> d;
                case String s -> Double.parseDouble(s);
                default -> throw new IllegalArgumentException("Invalid charge value");
            };
            
            userService.updateWorkerDetails(userId, workerType, charge);
            return ResponseEntity.ok("Details updated successfully");
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (NullPointerException e) {
            return ResponseEntity.badRequest().body("Required field missing: " + e.getMessage());
        }
    }

    @GetMapping("/incoming-requests")
    public List<ServiceRequest> getIncomingRequests(@RequestParam String userId) {
        return requestService.getIncomingRequests(userId);
    }

    @PostMapping("/accept-request")
    public ResponseEntity<String> acceptRequest(@RequestParam String requestId, @RequestParam String userId) {
        requestService.acceptRequest(requestId, userId);
        return ResponseEntity.ok("Request accepted");
    }

    @PostMapping("/decline-request")
    public ResponseEntity<String> declineRequest(@RequestParam String requestId, @RequestParam String userId) {
        requestService.declineRequest(requestId, userId);
        return ResponseEntity.ok("Request declined");
    }

    @GetMapping("/history")
    public ResponseEntity<List<ServiceRequest>> getHistory(
            @RequestParam String userId,
            @RequestParam(required = false, defaultValue = "all") String filter) {
        try {
            System.out.println("Getting history for worker: " + userId + ", filter: " + filter);
            List<ServiceRequest> history = requestService.getWorkerHistory(userId, filter);
            System.out.println("Found " + history.size() + " history items");
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}