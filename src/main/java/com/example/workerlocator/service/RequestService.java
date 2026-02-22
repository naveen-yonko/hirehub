// File: src/main/java/com/example/workerlocator/service/RequestService.java
// Service for handling requests.
package com.example.workerlocator.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.workerlocator.model.ServiceRequest;
import com.example.workerlocator.model.User;
import com.example.workerlocator.repository.ServiceRequestRepository;

@Service
public class RequestService {

    @Autowired
    private ServiceRequestRepository requestRepository;

    @Autowired
    private UserService userService;

    public ServiceRequest createRequest(ServiceRequest request) {
        request.setStatus("pending");
        return requestRepository.save(request);
    }

    public void acceptRequest(String requestId, String workerId) {
        Optional<ServiceRequest> optionalRequest = requestRepository.findById(requestId);
        if (optionalRequest.isPresent()) {
            ServiceRequest req = optionalRequest.get();
            // First check if request is pending and worker exists
            if ("pending".equals(req.getStatus())) {
                User worker = userService.getUserById(workerId);
                if (worker != null && worker.getCurrentJobId() == null) {
                    // Set workerId and status
                    req.setWorkerId(workerId);
                    req.setStatus("accepted");
                    worker.setCurrentJobId(requestId);
                    userService.updateAvailability(workerId, false); // set unavailable
                    requestRepository.save(req);
                    userService.saveUser(worker); // save worker

                    // Decline other pending requests for this worker
                    List<ServiceRequest> pendingRequests = requestRepository.findByWorkerIdAndStatus(workerId, "pending");
                    for (ServiceRequest pr : pendingRequests) {
                        if (!pr.getId().equals(requestId)) {
                            pr.setStatus("declined");
                            requestRepository.save(pr);
                        }
                    }
                }
            }
        }
    }

    public void declineRequest(String requestId, String workerId) {
        Optional<ServiceRequest> optionalRequest = requestRepository.findById(requestId);
        if (optionalRequest.isPresent()) {
            ServiceRequest req = optionalRequest.get();
            if ("pending".equals(req.getStatus()) && req.getWorkerId().equals(workerId)) {
                req.setStatus("declined");
                requestRepository.save(req);
            }
        }
    }

    public void completeRequest(String requestId, String customerId) {
        ServiceRequest req = requestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        // Validate request state
        if (!req.getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("Not authorized to complete this request");
        }
        if (!"accepted".equals(req.getStatus())) {
            throw new IllegalArgumentException("Request cannot be completed - current status: " + req.getStatus());
        }

        // Complete the request and update worker status
        try {
            req.setStatus("completed");
            requestRepository.save(req);

            // Update worker status
            User worker = userService.getUserById(req.getWorkerId());
            if (worker != null) {
                worker.setCurrentJobId(null);
                userService.updateAvailability(req.getWorkerId(), true);
                userService.saveUser(worker);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to complete request: " + e.getMessage(), e);
        }
    }

    public List<ServiceRequest> getIncomingRequests(String workerId) {
        return requestRepository.findByWorkerIdAndStatus(workerId, "pending");
    }

    public List<ServiceRequest> getWorkerHistory(String workerId, String filter) {
        return switch (filter) {
            case "completed" -> requestRepository.findByWorkerIdAndStatus(workerId, "completed");
            case "pending" -> requestRepository.findByWorkerIdAndStatus(workerId, "pending");
            default -> requestRepository.findByWorkerId(workerId);
        };
    }

    public List<ServiceRequest> getCustomerRequests(String customerId) {
        return requestRepository.findByCustomerId(customerId); // get all requests regardless of status
    }
}