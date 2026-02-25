package com.fptdemo.bookservice.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT UTILITY CLASS
 * 
 * JWT (JSON Web Token) EXPLANATION:
 * - JWT is a way to securely transmit information between parties
 * - In microservices, Auth Service creates JWT after login
 * - Other services (like Book Service) validate the JWT
 * 
 * JWT STRUCTURE:
 * Header.Payload.Signature
 * 
 * Example JWT:
 * eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyMTIzIiwicm9sZXMiOlsiVVNFUiJdLCJpYXQiOjE2MTYyMzkwMjJ9.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
 * 
 * JWT WORKFLOW IN MICROSERVICES:
 * 1. User logs in → Auth Service
 * 2. Auth Service validates credentials
 * 3. Auth Service creates JWT with user info (username, roles)
 * 4. Client stores JWT
 * 5. Client sends JWT with each request to Book Service
 * 6. Book Service validates JWT (this class does that)
 * 7. If valid, process request
 * 8. If invalid, return 401 Unauthorized
 * 
 * THIS CLASS DOES:
 * - Extract information from JWT (username, roles, expiration)
 * - Validate JWT signature
 * - Check if JWT is expired
 * 
 * @Component: Makes this a Spring bean (can be injected)
 */
@Component
@Slf4j
public class JwtUtil {

    /**
     * SECRET KEY for signing JWT
     * 
     * This comes from application.yml (jwt.secret)
     * 
     * IMPORTANT: In production, use a strong, unique secret!
     * Store it in environment variables, not in code!
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * JWT EXPIRATION TIME in milliseconds
     * 
     * This comes from application.yml (jwt.expiration)
     * Default: 86400000 ms = 24 hours
     */
    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * GET SECRET KEY
     * 
     * Converts string secret to SecretKey object for JWT signing
     * 
     * @return SecretKey for HMAC-SHA256 algorithm
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * EXTRACT USERNAME FROM JWT
     * 
     * JWT contains "claims" (pieces of information)
     * Subject claim usually contains username
     * 
     * @param token - JWT string
     * @return username
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * EXTRACT EXPIRATION DATE FROM JWT
     * 
     * @param token - JWT string
     * @return Expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * EXTRACT SPECIFIC CLAIM FROM JWT
     * 
     * Generic method to extract any claim
     * 
     * @param token - JWT string
     * @param claimsResolver - Function to extract specific claim
     * @return Extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * EXTRACT ALL CLAIMS FROM JWT
     * 
     * Parses JWT and extracts all claims
     * 
     * @param token - JWT string
     * @return All claims in the token
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())  // Verify signature
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("Failed to parse JWT token: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * CHECK IF TOKEN IS EXPIRED
     * 
     * Compares token expiration date with current date
     * 
     * @param token - JWT string
     * @return true if expired, false if still valid
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * VALIDATE JWT TOKEN
     * 
     * Checks if:
     * 1. Username in token matches username in UserDetails
     * 2. Token is not expired
     * 
     * @param token - JWT string
     * @param userDetails - User details from database/context
     * @return true if valid, false if invalid
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        boolean isValid = username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        
        if (isValid) {
            log.debug("JWT token is valid for user: {}", username);
        } else {
            log.warn("JWT token validation failed for user: {}", username);
        }
        
        return isValid;
    }

    /**
     * GENERATE JWT TOKEN
     * 
     * NOTE: In a microservices architecture, the Auth Service generates tokens.
     * This method is included for completeness but typically won't be used
     * by the Book Service.
     * 
     * @param userDetails - User details
     * @return Generated JWT token
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * CREATE JWT TOKEN
     * 
     * Builds JWT with:
     * - Claims (user info)
     * - Subject (username)
     * - Issued at time
     * - Expiration time
     * - Signature
     * 
     * @param claims - Additional claims to include
     * @param subject - Username
     * @return JWT string
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())  // Sign with secret key
                .compact();
    }
}
