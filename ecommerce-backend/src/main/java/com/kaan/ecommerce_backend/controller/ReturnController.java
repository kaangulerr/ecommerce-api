package com.kaan.ecommerce_backend.controller;

import com.kaan.ecommerce_backend.dto.ReturnRequestCreateDto;
import com.kaan.ecommerce_backend.dto.ReturnRequestDto;
import com.kaan.ecommerce_backend.entity.User;
import com.kaan.ecommerce_backend.repository.UserRepository;
import com.kaan.ecommerce_backend.service.ReturnRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/returns")
public class ReturnController {

    private final ReturnRequestService returnRequestService;
    private final UserRepository userRepository;

    public ReturnController(ReturnRequestService returnRequestService, UserRepository userRepository) {
        this.returnRequestService = returnRequestService;
        this.userRepository = userRepository;
    }

    private User getUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @PostMapping("/{orderId}")
    public ResponseEntity<?> createReturnRequest(
            Authentication authentication,
            @PathVariable Long orderId,
            @RequestBody ReturnRequestCreateDto dto) {
        try {
            return ResponseEntity.ok(returnRequestService.createReturnRequest(getUser(authentication), orderId, dto));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/my")
    public ResponseEntity<List<ReturnRequestDto>> getMyReturnRequests(Authentication authentication) {
        return ResponseEntity.ok(returnRequestService.getMyReturnRequests(getUser(authentication)));
    }
}
