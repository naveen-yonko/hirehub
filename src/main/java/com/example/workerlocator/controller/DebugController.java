package com.example.workerlocator.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.workerlocator.model.User;
import com.example.workerlocator.repository.UserRepository;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/all-workers")
    public List<User> getAllWorkers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                   .filter(user -> "worker".equals(user.getRole()))
                   .toList();
    }
}