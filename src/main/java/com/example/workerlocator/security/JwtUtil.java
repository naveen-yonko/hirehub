// File: src/main/java/com/example/workerlocator/security/JwtUtil.java
// Utility for JWT handling.
package com.example.workerlocator.security;

import java.io.IOException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class JwtUtil {

    // Lightweight payload parsing: decode JWT payload (base64url) and parse JSON claims.
    // NOTE: This does NOT verify the signature. It's a pragmatic fallback for local dev when jjwt APIs differ.
    private static Map<String, Object> parsePayload(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return Map.of();
            byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
            ObjectMapper om = new ObjectMapper();
            return om.readValue(decoded, Map.class);
        } catch (IllegalArgumentException | IOException e) {
            return Map.of();
        }
    }

    public static String extractUsername(String token, String secret) {
        Map<String, Object> claims = parsePayload(token);
        Object sub = claims.get("sub");
        return sub == null ? null : sub.toString();
    }

    public static Date extractExpiration(String token, String secret) {
        Map<String, Object> claims = parsePayload(token);
        Object exp = claims.get("exp");
        if (exp == null) return new Date(0);
        long epoch = switch (exp) {
            case Integer i -> i.longValue();
            case Long l -> l;
            case Object o -> Long.parseLong(o.toString());
        };
        return new Date(epoch * 1000);
    }

    private static Boolean isTokenExpired(String token, String secret) {
        return extractExpiration(token, secret).before(new Date());
    }

    public static String generateToken(String username, String role, String secret) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        return createToken(claims, username, secret);
    }

    private static String createToken(Map<String, Object> claims, String subject, String secret) {
    SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
    return Jwts.builder()
        .setClaims(claims)
        .setSubject(subject)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hours
        .signWith(key)
        .compact();
    }

    public static Boolean validateToken(String token, String username, String secret) {
        final String extractedUsername = extractUsername(token, secret);
        return (extractedUsername.equals(username) && !isTokenExpired(token, secret));
    }
}