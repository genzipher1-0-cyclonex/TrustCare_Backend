package com.cyclonex.trust_care.controller;

import com.cyclonex.trust_care.dto.AuthRequest;
import com.cyclonex.trust_care.dto.AuthResponse;
import com.cyclonex.trust_care.dto.RegisterRequest;
import com.cyclonex.trust_care.entity.Role;
import com.cyclonex.trust_care.entity.User;
import com.cyclonex.trust_care.repository.RoleRepository;
import com.cyclonex.trust_care.repository.UserRepository;
import com.cyclonex.trust_care.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody AuthRequest authRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getUsername(),
                            authRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtTokenProvider.generateToken(userDetails);

            User user = userRepository.findByUsername(authRequest.getUsername());
            String roleName = user.getRole() != null ? user.getRole().getRoleName() : "USER";

            return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), roleName));
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid username or password");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        try {
            // Check if username already exists
            if (userRepository.findByUsername(registerRequest.getUsername()) != null) {
                return ResponseEntity.badRequest().body("Username already exists");
            }

            // Create new user
            User user = new User();
            user.setUsername(registerRequest.getUsername());
            user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
            user.setStatus("active");

            // Set role
            Role role = roleRepository.findByRoleName(registerRequest.getRoleName());
            if (role == null) {
                return ResponseEntity.badRequest().body("Role not found");
            }
            user.setRole(role);

            userRepository.save(user);

            return ResponseEntity.ok("User registered successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error occurred during registration: " + e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username);
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.status(401).body("Not authenticated");
    }
}
