package com.example.classroom_assignment_management.controller;

import com.example.classroom_assignment_management.entity.User;
import com.example.classroom_assignment_management.entity.Role;
import com.example.classroom_assignment_management.repository.UserRepository;
import com.example.classroom_assignment_management.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authManager;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    // Login Endpoint
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        try {
            authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    authRequest.getUsername(), 
                    authRequest.getPassword()
                )
            );
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body("Invalid username or password");
        }

        User user = userRepository.findByUsername(authRequest.getUsername()).get();
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

        return ResponseEntity.ok(new AuthResponse(token));
    }

    // Registration Endpoint (Optional)
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        if(userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username is already taken");
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(registerRequest.getRole());

        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully");
    }
}

// DTOs
class AuthRequest {
    private String username;
    private String password;

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}

class AuthResponse {
    private String token;

    public AuthResponse(String token) { this.token = token; }

    // Getters and Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}

class RegisterRequest {
    private String username;
    private String password;
    private Role role;

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
