package com.kaan.ecommerce_backend.controller;

import com.kaan.ecommerce_backend.dto.UserProfileDto;
import com.kaan.ecommerce_backend.service.CloudinaryService;
import com.kaan.ecommerce_backend.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final CloudinaryService cloudinaryService;

    public UserController(UserService userService, CloudinaryService cloudinaryService) {
        this.userService = userService;
        this.cloudinaryService = cloudinaryService;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDto> getProfile(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(userService.getProfile(email));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileDto> updateProfile(
            Authentication authentication,
            @RequestBody UserProfileDto profileDto) {
        String email = authentication.getName();
        return ResponseEntity.ok(userService.updateProfile(email, profileDto));
    }

    @PostMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)

    public ResponseEntity<String> uploadProfileImage(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) throws IOException {
        
        String email = authentication.getName();
        String photoURL = cloudinaryService.uploadImage(file);
        userService.updatePhotoURL(email, photoURL);
        
        return ResponseEntity.ok(photoURL);
    }
}
