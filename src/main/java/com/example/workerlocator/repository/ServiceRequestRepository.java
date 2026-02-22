// File: src/main/java/com/example/workerlocator/repository/ServiceRequestRepository.java
// ServiceRequest repository.
package com.example.workerlocator.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.workerlocator.model.ServiceRequest;

public interface ServiceRequestRepository extends MongoRepository<ServiceRequest, String> {
    List<ServiceRequest> findByWorkerIdAndStatus(String workerId, String status);
    List<ServiceRequest> findByCustomerIdAndStatus(String customerId, String status);
    List<ServiceRequest> findByWorkerId(String workerId);
    List<ServiceRequest> findByCustomerId(String customerId);
}