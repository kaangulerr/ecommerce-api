
package com.kaan.ecommerce_backend.controller;

import com.kaan.ecommerce_backend.dto.ProductDetailDto;
import com.kaan.ecommerce_backend.dto.ProductDto;
import com.kaan.ecommerce_backend.dto.ReviewDto;
import com.kaan.ecommerce_backend.dto.ReviewRequest;
import com.kaan.ecommerce_backend.entity.User;
import com.kaan.ecommerce_backend.repository.ProductRepository;
import com.kaan.ecommerce_backend.repository.UserRepository;
import com.kaan.ecommerce_backend.service.AiBridgeService;
import com.kaan.ecommerce_backend.service.ProductService;
import com.kaan.ecommerce_backend.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final ReviewService reviewService;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final AiBridgeService aiBridgeService;

    public ProductController(ProductService productService,
            ReviewService reviewService,
            UserRepository userRepository,
            ProductRepository productRepository,
            AiBridgeService aiBridgeService) {
        this.productService = productService;
        this.reviewService = reviewService;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.aiBridgeService = aiBridgeService;
    }

    private Long getAuthenticatedUserId(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof User) {
            return ((User) principal).getId();
        }

        String email = null;
        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            email = (String) principal;
        }

        if (email != null) {
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent())
                return userOpt.get().getId();
        }

        return null;
    }

    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            Authentication authentication) {
        return ResponseEntity.ok(
                productService.getAllProducts(getAuthenticatedUserId(authentication), page, size));
    }

    @GetMapping("/{asin}")
    public ResponseEntity<ProductDetailDto> getProductDetail(
            @PathVariable String asin,
            Authentication authentication) {
        return ResponseEntity.ok(
                productService.getProductDetail(asin, getAuthenticatedUserId(authentication)));
    }

    /**
     * GET /api/products/{asin}/reviews
     * Herkese açık – anonim kullanıcılar da görebilir.
     */
    @GetMapping("/{asin}/reviews")
    public ResponseEntity<List<ReviewDto>> getProductReviews(
            @PathVariable String asin,
            Authentication authentication) {
        return ResponseEntity.ok(
                reviewService.getReviewsByAsin(asin, getAuthenticatedUserId(authentication)));
    }

    /**
     * POST /api/products/{asin}/reviews
     * Sadece giriş yapmış kullanıcılar yorum yapabilir.
     */
    @PostMapping("/{asin}/reviews")
    public ResponseEntity<?> addReview(
            @PathVariable String asin,
            @RequestBody @Valid ReviewRequest reviewRequest,
            Authentication authentication) {
        try {
            Long userId = getAuthenticatedUserId(authentication);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User session not found.");
            }
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(reviewService.addReview(asin, userId, reviewRequest));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Review Error: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductDto>> searchProducts(
            @RequestParam(value = "q", required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            Authentication authentication) {

        Long userId = getAuthenticatedUserId(authentication);
        if (keyword == null || keyword.isBlank()) {
            return ResponseEntity.ok(productService.getAllProducts(userId, page, size));
        }
        return ResponseEntity.ok(productService.searchProducts(keyword.trim(), userId, page, size));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<ProductDto>> filterProducts(
            @RequestParam(value = "mainCategory", required = false) String mainCategory,
            @RequestParam(value = "subCategory", required = false) String subCategory,
            @RequestParam(value = "brand", required = false) String brand,
            @RequestParam(value = "color", required = false) String color,
            @RequestParam(value = "size", required = false) String productSize,
            @RequestParam(value = "minRating", required = false) String minRating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(value = "pageSize", defaultValue = "2147483647") int pageSize,
            Authentication authentication) {

        return ResponseEntity.ok(productService.filterProducts(
                mainCategory, subCategory, brand, color, productSize, minRating,
                getAuthenticatedUserId(authentication), page, pageSize));
    }

    @PostMapping("/admin/auto-categorize")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<String> autoCategorizeProducts() {
        return ResponseEntity.ok(productService.categorizeExistingProducts());
    }

    /**
     * GET /api/products/asin/{asin}/ai-summary
     * Ürün yorumlarını AI ile özetler.
     */
    @GetMapping("/asin/{asin}/ai-summary")
    public ResponseEntity<String> getAiSummary(@PathVariable String asin) {
        var productOpt = productRepository.findByAsin(asin);
        if (productOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found.");
        }

        var product = productOpt.get();

        if (product.getReviews() == null || product.getReviews().isEmpty()) {
            return ResponseEntity.ok("No reviews yet");
        }

        List<String> reviewTexts = product.getReviews().stream()
                .map(review -> review.getReviewtext())
                .filter(text -> text != null && !text.isBlank())
                .collect(java.util.stream.Collectors.toList());

        if (reviewTexts.isEmpty()) {
            return ResponseEntity.ok("No reviews yet");
        }

        String summary = aiBridgeService.analyzeReviews(reviewTexts);

        return ResponseEntity.ok(summary);
    }
}