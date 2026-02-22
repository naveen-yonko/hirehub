// File: src/main/java/com/example/workerlocator/controller/AuthController.java
// Controller for authentication.
package com.example.workerlocator.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.workerlocator.model.User;
import com.example.workerlocator.security.JwtUtil;
import com.example.workerlocator.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Value("${jwt.secret}")
    private String secret;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        User savedUser = userService.registerUser(user);
        return ResponseEntity.ok(savedUser);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        try {
            String email = loginData.get("email");
            String password = loginData.get("password");
            String role = loginData.get("role");
            
            if (email == null || password == null || role == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Email, password and role are required"));
            }

            User user = userService.loginUser(email, password, role);
            String token = JwtUtil.generateToken(user.getEmail(), user.getRole(), secret);
            
            return ResponseEntity.ok(Map.of(
                "token", token,
                "userId", user.getId(),
                "role", user.getRole(),
                "email", user.getEmail()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "An unexpected error occurred"));
        }
    }
}