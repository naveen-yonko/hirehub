// File: src/main/java/com/example/workerlocator/repository/UserRepository.java
// User repository.
package com.example.workerlocator.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.workerlocator.model.User;

public interface UserRepository extends MongoRepository<User, String> {
    User findByEmail(String email);
    List<User> findByRole(String role);
}