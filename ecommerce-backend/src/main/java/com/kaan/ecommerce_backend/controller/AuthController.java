package com.kaan.ecommerce_backend.controller;

import com.kaan.ecommerce_backend.dto.AuthRequestDto;
import com.kaan.ecommerce_backend.dto.AuthResponse;
import com.kaan.ecommerce_backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequestDto request) {
        AuthResponse response = userService.loginOrRegister(
                request.getEmail(),
                request.getFirstName(),
                request.getLastName(),
                request.getAuthProvider()
        );

        return ResponseEntity.ok(response);
    }
}