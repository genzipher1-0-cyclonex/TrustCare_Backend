package com.cyclonex.trust_care.controller;

import com.cyclonex.trust_care.dto.*;
import com.cyclonex.trust_care.entity.Role;
import com.cyclonex.trust_care.entity.User;
import com.cyclonex.trust_care.repository.RoleRepository;
import com.cyclonex.trust_care.repository.UserRepository;
import com.cyclonex.trust_care.security.JwtTokenProvider;
import com.cyclonex.trust_care.service.EmailService;
import com.cyclonex.trust_care.service.OtpService;
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

    @Autowired
    private OtpService otpService;

    @Autowired
    private EmailService emailService;

    /**
     * Step 1: Initiate login - Verify credentials and send OTP to email
     */
    @PostMapping("/login")
    public ResponseEntity<?> initiateLogin(@RequestBody AuthRequest authRequest) {
        try {
            // Authenticate user credentials
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getEmail(),
                            authRequest.getPassword()
                    )
            );

            // Get user details
            User user = userRepository.findByEmail(authRequest.getEmail());
            if (user == null) {
                return ResponseEntity.status(401).body("User not found");
            }

            // Check if user has email
            if (user.getEmail() == null || user.getEmail().isEmpty()) {
                return ResponseEntity.badRequest().body("User does not have an email address. Please contact administrator.");
            }

            // Generate OTP
            String otp = otpService.generateOtp();
            
            // Store OTP
            otpService.storeOtp(user.getUsername(), otp);
            
            // Send OTP to email
            emailService.sendOtpEmail(user.getEmail(), otp, user.getUsername());
            
            // Return response
            return ResponseEntity.ok(new LoginInitiateResponse(
                    "OTP sent to your email. Please verify to complete login.",
                    user.getUsername(),
                    true,
                    emailService.maskEmail(user.getEmail())
            ));

        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid email or password");
        }
    }

    /**
     * Step 2: Verify OTP and complete login - Return JWT token
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpVerificationRequest request) {
        try {
            // Verify OTP
            boolean isValid = otpService.verifyOtp(request.getUsername(), request.getOtp());
            
            if (!isValid) {
                return ResponseEntity.status(401).body("Invalid or expired OTP");
            }

            // Get user details
            User user = userRepository.findByUsername(request.getUsername());
            if (user == null) {
                return ResponseEntity.status(401).body("User not found");
            }

            // Generate JWT token
            UserDetails userDetails = org.springframework.security.core.userdetails.User
                    .withUsername(user.getEmail())
                    .password(user.getPasswordHash())
                    .authorities("ROLE_" + (user.getRole() != null ? user.getRole().getRoleName() : "USER"))
                    .build();

            String token = jwtTokenProvider.generateToken(userDetails);
            String roleName = user.getRole() != null ? user.getRole().getRoleName() : "USER";

            return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), roleName));

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error occurred during OTP verification: " + e.getMessage());
        }
    }

    /**
     * Resend OTP to user's email
     */
    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestBody String username) {
        try {
            User user = userRepository.findByUsername(username);
            if (user == null) {
                return ResponseEntity.status(404).body("User not found");
            }

            if (user.getEmail() == null || user.getEmail().isEmpty()) {
                return ResponseEntity.badRequest().body("User does not have an email address");
            }

            // Generate new OTP
            String otp = otpService.generateOtp();
            
            // Store OTP
            otpService.storeOtp(user.getUsername(), otp);
            
            // Send OTP to email
            emailService.sendOtpEmail(user.getEmail(), otp, user.getUsername());
            
            return ResponseEntity.ok(new LoginInitiateResponse(
                    "New OTP sent to your email",
                    user.getUsername(),
                    true,
                    emailService.maskEmail(user.getEmail())
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error occurred while resending OTP: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        try {
            // Check if email already exists
            if (userRepository.findByEmail(registerRequest.getEmail()) != null) {
                return ResponseEntity.badRequest().body("Email already exists");
            }

            // Validate email
            if (registerRequest.getEmail() == null || registerRequest.getEmail().isEmpty()) {
                return ResponseEntity.badRequest().body("Email is required");
            }

            // Create new user
            User user = new User();
            user.setUsername(registerRequest.getUsername());
            user.setEmail(registerRequest.getEmail());
            user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
            user.setStatus("active");

            // Set role
            Role role = roleRepository.findByRoleName(registerRequest.getRoleName());
            if (role == null) {
                return ResponseEntity.badRequest().body("Role not found");
            }
            user.setRole(role);

            userRepository.save(user);

            // Send welcome email
            emailService.sendRegistrationEmail(user.getEmail(), user.getUsername());

            return ResponseEntity.ok("User registered successfully. You can now login with OTP verification.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error occurred during registration: " + e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.status(401).body("Not authenticated");
    }
}
