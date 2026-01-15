package com.epoll.server_v1.security;

import com.epoll.server_v1.security.dto.LoginRequest;
import com.epoll.server_v1.security.dto.RegisterRequest;
import com.epoll.server_v1.security.dto.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UsersController {

    private final UserService userService;

    public UsersController(UserService userService) {
        this.userService = userService;
    }

    // ======================
    // Registration
    // ======================

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {

        Users user = new Users();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(request.getPassword());

        Users saved = userService.registerUser(user);

        return ResponseEntity.ok(
                new UserResponse(
                        saved.getId(),
                        saved.getFirstName(),
                        saved.getLastName(),
                        saved.getEmail()
                )
        );

    }

    // ======================
    // Login
    // ======================

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        String token = userService.authenticate(
                request.getEmail(),
                request.getPassword()
        );

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("message", "Login successful");

        return ResponseEntity.ok(response);
    }

    // ======================
    // Current user
    // ======================

    @GetMapping("/me")
    public ResponseEntity<UserResponse> currentUser(Authentication authentication) {

        Users user = userService.findByEmail(authentication.getName());

        return ResponseEntity.ok(
                new UserResponse(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail()
                )
        );
    }
}
