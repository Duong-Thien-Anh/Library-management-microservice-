package com.fptdemo.bookservice.security;

import java.io.IOException;
import java.util.stream.Collectors;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT AUTHENTICATION FILTER
 * 
 * FILTER EXPLANATION:
 * - A filter intercepts every HTTP request before it reaches the controller
 * - This filter extracts and validates JWT from request headers
 * - If JWT is valid, user is authenticated
 * - If JWT is invalid/missing, request is rejected
 * 
 * REQUEST FLOW:
 * 1. Client sends request with header: Authorization: Bearer <JWT_TOKEN>
 * 2. This filter intercepts the request
 * 3. Filter extracts JWT from Authorization header
 * 4. Filter validates JWT using JwtUtil
 * 5. If valid, creates Authentication object and stores in SecurityContext
 * 6. Request continues to controller
 * 7. Controller can access authenticated user info
 * 
 * OncePerRequestFilter:
 * - Ensures filter runs exactly once per request
 * - Handles all requests uniformly
 * 
 * @Component: Makes this a Spring bean
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    /**
     * FILTER METHOD
     * 
     * This method is called for EVERY request to the Book Service
     * 
     * STEPS:
     * 1. Extract JWT from Authorization header
     * 2. Parse JWT to get username
     * 3. Validate JWT
     * 4. If valid, set authentication in SecurityContext
     * 5. Continue with request
     * 
     * @param request - HTTP request
     * @param response - HTTP response
     * @param filterChain - Chain of filters
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        //  Get Authorization header
        // Example: "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        String authHeader = request.getHeader("Authorization");
        
        String username = null;
        String jwt = null;

        //  Extract JWT token
        // Check if header starts with "Bearer "
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);  // Remove "Bearer " prefix
            try {
                username = jwtUtil.extractUsername(jwt);
                log.debug("JWT token found for user: {}", username);
            } catch (Exception e) {
                log.error("Failed to extract username from JWT: {}", e.getMessage());
            }
        }

        //  Validate JWT and set authentication
        // If username is extracted and no authentication exists yet
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            try {
                // In a real microservices setup, you might call User Service here
                // For now, we'll create a simple UserDetails from JWT
                
                // Extract roles from JWT (Auth Service should include roles in JWT)
                // For this example, we'll assume roles are in a "roles" claim
                Object rolesClaim = jwtUtil.extractClaim(jwt, claims -> claims.get("roles"));
                java.util.List<String> roles = new java.util.ArrayList<>();

                if (rolesClaim instanceof String s) {
                    roles.add(s);
                } else if (rolesClaim instanceof java.util.List) {
                    @SuppressWarnings("unchecked")
                    java.util.List<String> rolesList = (java.util.List<String>) rolesClaim;
                    roles.addAll(rolesList);
                }

              
                java.util.List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

               
                if (authorities.isEmpty()) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                }
                
                // Create UserDetails object
                UserDetails userDetails = User.builder()
                        .username(username)
                        .password("")  // Password not needed for token-based auth
                        .authorities(authorities)
                        .build();

                // Validate token
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    // STEP 4: Create Authentication object
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,  // Credentials not needed
                            userDetails.getAuthorities()
                        );
                    
                    authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    
                    // Set authentication in SecurityContext
                    // This tells Spring Security that user is authenticated
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    log.info("User '{}' authenticated successfully with roles: {}", 
                             username, roles);
                } else {
                    log.warn("JWT token validation failed for user: {}", username);
                }
                
            } catch (Exception e) {
                log.error("Cannot set user authentication: {}", e.getMessage());
            }
        }

        //  Continue with the request
        // Pass to next filter in chain or to controller
        filterChain.doFilter(request, response);
    }
}
