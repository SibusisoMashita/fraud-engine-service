package com.fraudengine.api;

import com.fraudengine.security.JwtService;
import com.fraudengine.security.dto.JwtResponse;
import com.fraudengine.security.dto.LoginRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;

@RestController
public class AuthController {

    private final JwtService jwtService;

    public AuthController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping("/auth/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        if (!"admin".equals(request.getUsername()) || !"password123".equals(request.getPassword())) {
            return ResponseEntity.status(401).build();
        }
        String token = jwtService.generateToken(request.getUsername(), Duration.ofHours(1));
        long expiresAt = Instant.now().plus(Duration.ofHours(1)).toEpochMilli();
        JwtResponse response = JwtResponse.builder()
                .token(token)
                .type("Bearer")
                .expiresAt(expiresAt)
                .build();
        return ResponseEntity.ok(response);
    }
}
