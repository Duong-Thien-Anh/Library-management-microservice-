package com.fptdemo.authservice.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    /**
     * PASSWORD ENCODER
     *
     * BCrypt is the industry standard for storing passwords.
     * BCrypt automatically:
     *   - Adds a random "salt" (prevents rainbow table attacks)
     *   - Applies slow hashing (makes brute-force harder)
     *   - Generates a different hash each time even for the same password
     *
     * Usage in AuthService:
     *   String hash = passwordEncoder.encode("mypassword");
     *   boolean matches = passwordEncoder.matches("mypassword", hash);  // true
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AUTHENTICATION PROVIDER
     *
     * DaoAuthenticationProvider is Spring's standard provider that:
     *   1. Calls loadUserByUsername() on your CustomUserDetailsService
     *   2. Uses BCrypt to compare the provided password with the stored hash
     *   3. Returns an Authentication object if credentials are correct
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * AUTHENTICATION MANAGER
     *
     * This is the entry point for authentication.
     * You inject this into AuthService and call:
     *   authenticationManager.authenticate(
     *       new UsernamePasswordAuthenticationToken(username, password)
     *   );
     * Spring Security handles the rest.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * SECURITY FILTER CHAIN
     *
     * For auth-service:
     * - /api/auth/register → PUBLIC (no token required, user doesn't have one yet)
     * - /api/auth/login    → PUBLIC (same reason)
     * - Anything else      → require authentication (for future admin endpoints)
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF — not needed for stateless REST APIs
                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth
                        // These endpoints must be open — the user has no token yet!
                        .requestMatchers(
                                "/v1/register",
                                "/api/auth/**",
                                "/v1/login",
                                "/v1/**",
                                "/api/auth/v1/**",
                                "/actuator/**",
                                "/error"
                        ).permitAll()

                        // All other requests require a valid JWT
                        .anyRequest().authenticated()
                )

                // STATELESS: no sessions, no cookies — each request is independent
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authenticationProvider(authenticationProvider());

        return http.build();
    }
}
