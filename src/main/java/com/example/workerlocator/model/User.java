// File: src/main/java/com/example/workerlocator/model/User.java
// User model.
package com.example.workerlocator.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.geo.Point;

import java.util.List;

@Document(collection = "users")
public class User {
    @Id
    private String id;
    private String email;
    private String password;
    private String role; // "customer" or "worker"
    @GeoSpatialIndexed
    private Point location; // for workers, primary location
    private List<Point> additionalLocations; // for customers
    private String workerType;
    private double charge;
    private boolean available;
    private String currentJobId;
    private double averageRating;
    private int ratingCount;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public List<Point> getAdditionalLocations() {
        return additionalLocations;
    }

    public void setAdditionalLocations(List<Point> additionalLocations) {
        this.additionalLocations = additionalLocations;
    }

    public String getWorkerType() {
        return workerType;
    }

    public void setWorkerType(String workerType) {
        this.workerType = workerType;
    }

    public double getCharge() {
        return charge;
    }

    public void setCharge(double charge) {
        this.charge = charge;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getCurrentJobId() {
        return currentJobId;
    }

    public void setCurrentJobId(String currentJobId) {
        this.currentJobId = currentJobId;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }
}