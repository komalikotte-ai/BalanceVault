package com.ledgerline.controller;

import com.ledgerline.dto.AuthMeResponse;
import com.ledgerline.dto.LoginRequest;
import com.ledgerline.dto.LoginResponse;
import com.ledgerline.dto.RegisterRequest;
import com.ledgerline.dto.RegisterResponse;
import com.ledgerline.entity.AppUser;
import com.ledgerline.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        AppUser user = authService.register(request);

        RegisterResponse response = new RegisterResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name(),
                user.getStatus().name()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthMeResponse> getCurrentUser(
            @AuthenticationPrincipal Jwt jwt) {

        AuthMeResponse response = new AuthMeResponse(
                UUID.fromString(jwt.getClaimAsString("userId")),
                jwt.getSubject(),
                jwt.getClaimAsString("role")
        );

        return ResponseEntity.ok(response);
    }
}