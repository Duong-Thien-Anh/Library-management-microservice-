package com.fptdemo.bookservice.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

/**
 * SECURITY CONFIGURATION
 * 
 * SECURITY CONFIGURATION EXPLANATION:
 * - This class configures how Spring Security protects the Book Service
 * - Defines which endpoints require authentication
 * - Integrates JWT authentication
 * - Disables session-based authentication (stateless for microservices)
 * 
 * MICROSERVICES SECURITY:
 * - Each microservice validates JWT independently
 * - No shared sessions between services
 * - Stateless authentication (JWT contains all needed info)
 * 
 * @Configuration: Marks this as a configuration class
 * @EnableWebSecurity: Enables Spring Security
 * @EnableMethodSecurity: Enables method-level security (@PreAuthorize)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // Enables @PreAuthorize in controllers
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * SECURITY FILTER CHAIN
     * 
     * This method configures the security rules for the Book Service
     * 
     * CONFIGURATION STEPS:
     * 1. Disable CSRF (not needed for stateless JWT)
     * 2. Configure authorization rules
     * 3. Configure session management (stateless)
     * 4. Add JWT filter
     * 
     * @param http - HttpSecurity object to configure
     * @return SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // DISABLE CSRF
            // CSRF (Cross-Site Request Forgery) protection not needed for stateless APIs
            // Since we use JWT (not cookies), we're safe from CSRF attacks
            .csrf(AbstractHttpConfigurer::disable)
            
            // CONFIGURE AUTHORIZATION RULES
            .authorizeHttpRequests(auth -> auth
                // PUBLIC ENDPOINTS (no authentication required)
                .requestMatchers(
                    "/actuator/**",     // Health checks
                    "/v1/health",       // Custom health endpoint
                    "/error"           // Error endpoint     
                ).permitAll()
                
                // AUTHENTICATED ENDPOINTS (require valid JWT)
                // All other endpoints under /v1/** require authentication
                // Specific role requirements are handled by @PreAuthorize in controllers
                .requestMatchers("/v1/**").authenticated()
                
                // ALL OTHER REQUESTS require authentication
                .anyRequest().authenticated()
            )
            
            // SESSION MANAGEMENT (STATELESS)
            // Microservices should be stateless
            // Don't create HTTP sessions
            // Each request must include JWT
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            //  ADD JWT FILTER
            // Add our custom JWT filter before Spring Security's authentication filter
            // This ensures JWT is processed before Spring checks authentication
            .addFilterBefore(
                jwtAuthenticationFilter, 
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    /**
     * AUTHENTICATION FLOW EXPLANATION:
     * 
     * 1. Client sends request with JWT in Authorization header
     *    Example: Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
     * 
     * 2. JwtAuthenticationFilter intercepts request
     *    - Extracts JWT from header
     *    - Validates JWT signature
     *    - Checks expiration
     *    - Extracts username and roles
     *    - Creates Authentication object
     *    - Stores in SecurityContext
     * 
     * 3. Request reaches Controller
     *    - @PreAuthorize checks user's roles
     *    - If authorized, method executes
     *    - If not authorized, returns 403 Forbidden
     * 
     * 4. Response sent back to client
     * 
     * 5. SecurityContext cleared after request
     *    (stateless - no session stored)
     */
}
