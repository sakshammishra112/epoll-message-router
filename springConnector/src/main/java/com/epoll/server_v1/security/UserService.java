package com.epoll.server_v1.security;

import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    private final UserRepo userRepo;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepo userRepo,
            AuthenticationManager authenticationManager,
            JWTService jwtService,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepo = userRepo;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    // ======================
    // Queries
    // ======================

    public List<Users> getAllUsers() {
        return userRepo.findAll();
    }

    public Users findByEmail(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    // ======================
    // Registration
    // ======================

    @Transactional
    public Users registerUser(Users user) {

        if (userRepo.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        return userRepo.save(user);
    }

    // ======================
    // Authentication
    // ======================

    public String authenticate(String email, String rawPassword) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, rawPassword)
        );

        Users user = findByEmail(email);
        user.setLastLoginAt(LocalDateTime.now());

        return jwtService.generateToken(
                (UserPrincipal) authentication.getPrincipal()
        );
    }

    public Users findById(long from) {
        return userRepo.findById(from)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public boolean existsById(Long id) {
        return userRepo.existsById(id);
    }

}
