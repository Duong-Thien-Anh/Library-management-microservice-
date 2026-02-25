package com.fptdemo.authservice.security;

import com.fptdemo.authservice.entity.User;
import com.fptdemo.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);

        // Fetch user from DB — throw exception if not found
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });

        // Spring Security convention: roles must be prefixed with "ROLE_"
        // e.g., USER → ROLE_USER,  ADMIN → ROLE_ADMIN
        String roleWithPrefix = "ROLE_" + user.getRole().name();

        // org.springframework.security.core.userdetails.User (Spring's built-in UserDetails)
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())         // already BCrypt hashed
                .authorities(new SimpleGrantedAuthority(roleWithPrefix))
                .disabled(!user.isEnabled())
                .build();
    }
}