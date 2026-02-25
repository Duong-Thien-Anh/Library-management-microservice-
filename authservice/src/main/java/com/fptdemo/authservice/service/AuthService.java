package com.fptdemo.authservice.service;

import com.fptdemo.authservice.dto.AuthResponseDTO;
import com.fptdemo.authservice.dto.LoginRequestDTO;
import com.fptdemo.authservice.dto.RegisterRequestDTO;
import com.fptdemo.authservice.entity.Role;
import com.fptdemo.authservice.entity.User;
import com.fptdemo.authservice.exception.InvalidCredentialsException;
import com.fptdemo.authservice.exception.UserAlreadyExistsException;
import com.fptdemo.authservice.repository.UserRepository;
import com.fptdemo.authservice.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;              // BCrypt from SecurityConfig
    private final AuthenticationManager authenticationManager; // from SecurityConfig
    private final JwtUtil jwtUtil;

    /**
     * REGISTER
     * @param request  the incoming RegisterRequestDTO
     * @return AuthResponseDTO with JWT token
     */
    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        log.info("Registering new user: {}", request.getUsername());

        // Check username uniqueness
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException(
                    "Username already taken: " + request.getUsername()
            );
        }

        // Check email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(
                    "Email already registered: " + request.getEmail()
            );
        }

        // Hash the password
        // NEVER store plain text: "mypassword" → "$2a$10$abc..."
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // Build the User entity using Lombok @Builder
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(hashedPassword)    // store hash, NOT the plain password
                .role(Role.USER)             // new users always start as USER
                .enabled(true)
                .build();

        userRepository.save(user);
        log.info("User saved with id: {}", user.getId());

        // Generate token and return
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

        return AuthResponseDTO.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().name())
                .expiresIn(86400000L)
                .build();
    }

    /**
     * LOGIN
     * @param request  the incoming LoginRequestDTO
     * @return AuthResponseDTO with JWT token
     */
    public AuthResponseDTO login(LoginRequestDTO request) {
        log.info("Login attempt for user: {}", request.getUsername());
        try {
            // Authenticate — Spring Security does the heavy lifting
            // This internally calls: loadUserByUsername → BCrypt.matches
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            // Wrong password or unknown username
            log.warn("Failed login attempt for user: {}", request.getUsername());
            throw new InvalidCredentialsException("Invalid username or password");
        }

        // Credentials are correct — fetch user to get role
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        // Issue JWT
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        log.info("Login successful for user: {}", user.getUsername());

        return AuthResponseDTO.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().name())
                .expiresIn(86400000L)
                .build();


    }
}
