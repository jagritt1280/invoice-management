package com.invoice.controller;

import com.invoice.dto.*;
import com.invoice.entity.User;
import com.invoice.exception.DuplicateEmailException;
import com.invoice.repository.UserRepository;
import com.invoice.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    // ── REGISTER ───────────────────────────────────────────────
    // POST /auth/register
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        // check duplicate email
        if(userRepository.existsByEmail(request.getEmail()))
            throw new DuplicateEmailException(request.getEmail());

        // create user with hashed password
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                // passwordEncoder.encode() → BCrypt hash
                // NEVER store plain text password
                .build();

        userRepository.save(user);

        // generate JWT token
        String token = jwtUtil.generateToken(user.getEmail());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful",
                        new AuthResponse(token, user.getName(),
                                user.getEmail())));
    }

    // ── LOGIN ──────────────────────────────────────────────────
    // POST /auth/login
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        // authenticate — Spring checks email + password
        // throws BadCredentialsException if wrong
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));

        // if we reach here — credentials are correct
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        String token = jwtUtil.generateToken(user.getEmail());

        return ResponseEntity.ok(
                ApiResponse.success("Login successful",
                        new AuthResponse(token, user.getName(),
                                user.getEmail())));
    }
}