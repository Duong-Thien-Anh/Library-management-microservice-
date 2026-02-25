package com.fptdemo.authservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;       // Loaded from application.yml

    @Value("${jwt.expiration}")
    private Long expiration;     // Milliseconds (86400000 = 24 hours)

    /**
     * CONVERT the plain string secret → SecretKey object for HMAC-SHA256
     *
     * WHY? The JJWT library requires a SecretKey, not a raw String.
     * Keys.hmacShaKeyFor() ensures the key is the right size for HS256.
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * GENERATE TOKEN
     *
     * Called after successful login. Builds a signed JWT containing:
     *   - username (subject)
     *   - role (custom claim)
     *   - issue time
     *   - expiry time
     *
     * @param username  the authenticated user's username
     * @param role      the user's role ("USER", "ADMIN", "LIBRARIAN")
     * @return signed JWT string
     */
    public String generateToken(String username, String role) {
        log.debug("Generating JWT for user: {}", username);

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("roles", role);  // ← book-service reads this claim

        return Jwts.builder()
                .claims(extraClaims)                          // custom claims first
                .subject(username)                            // "sub" claim
                .issuedAt(new Date())                         // "iat" claim
                .expiration(new Date(System.currentTimeMillis() + expiration))  // "exp" claim
                .signWith(getSigningKey())                    // sign with HS256
                .compact();                                   // build the string
    }

    /**
     * EXTRACT USERNAME from a token
     * Useful for introspection endpoints (validate token)
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * CHECK if a token is expired
     */
    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    /**
     * VALIDATE TOKEN
     * Returns true if the token is signed correctly and not expired.
     */
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);  // throws exception if invalid
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * PARSE the token and return all claims (the payload section)
     * Throws an exception if the signature doesn't match or token is malformed.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())   // verify signature
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
