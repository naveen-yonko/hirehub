package com.example.workerlocator.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.workerlocator.model.User;
import com.example.workerlocator.repository.UserRepository;
import java.util.List;

@RestController
public class TestController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/api/test/list-workers")
    public List<User> listAllWorkers() {
        return userRepository.findByRole("worker");
    }
}