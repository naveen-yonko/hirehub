package com.example.workerlocator.config;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.index.GeospatialIndex;

import jakarta.annotation.PostConstruct;

@Configuration
public class MongoConfig {
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    @PostConstruct
    public void initIndexes() {
        // Create a geospatial index on the location field
        mongoTemplate.indexOps("users").ensureIndex(new GeospatialIndex("location"));
        
        // Create a compound index for location and workerType
        Document index = new Document();
        index.put("location", "2dsphere");
        index.put("workerType", 1);
        index.put("available", 1);
        mongoTemplate.indexOps("users").ensureIndex(new CompoundIndexDefinition(index));
    }
}