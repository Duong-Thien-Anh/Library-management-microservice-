package com.fptdemo.authservice.repository;

import com.fptdemo.authservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Used by Spring Security to load user during login
    Optional<User> findByUsername(String username);

    // Used during registration to check if username is already taken
    boolean existsByUsername(String username);

    // Used during registration to check if email is already registered
    boolean existsByEmail(String email);
}
