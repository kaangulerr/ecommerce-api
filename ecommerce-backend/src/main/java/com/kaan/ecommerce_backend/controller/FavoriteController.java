package com.kaan.ecommerce_backend.controller;

import com.kaan.ecommerce_backend.dto.FavoriteDto;
import com.kaan.ecommerce_backend.service.FavoriteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@CrossOrigin(origins = "*")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/toggle/{asin}")
    public ResponseEntity<String> toggleFavorite(@PathVariable String asin, Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(401).body("Please log in first!");
        }

        String userEmail = principal.getName();
        String resultMessage = favoriteService.toggleFavorite(userEmail, asin);

        return ResponseEntity.ok(resultMessage);
    }

    @GetMapping
    public ResponseEntity<List<FavoriteDto>> getMyFavorites(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        String userEmail = principal.getName();
        List<FavoriteDto> myFavorites = favoriteService.getUserFavorites(userEmail);

        return ResponseEntity.ok(myFavorites);
    }
}