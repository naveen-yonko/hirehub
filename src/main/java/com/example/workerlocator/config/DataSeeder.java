// File: src/main/java/com/example/workerlocator/config/DataSeeder.java
package com.example.workerlocator.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.workerlocator.model.User;
import com.example.workerlocator.repository.UserRepository;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner seedUsers(UserRepository userRepository, PasswordEncoder encoder) {
        return args -> {
            if (userRepository.findByEmail("customer@example.com") == null) {
                User u = new User();
                u.setEmail("customer@example.com");
                u.setRole("customer");
                u.setPassword(encoder.encode("password"));
                userRepository.save(u);
            }

            if (userRepository.findByEmail("worker@example.com") == null) {
                User w = new User();
                w.setEmail("worker@example.com");
                w.setRole("worker");
                w.setPassword(encoder.encode("password"));
                w.setAvailable(true);
                userRepository.save(w);
            }
        };
    }
}
