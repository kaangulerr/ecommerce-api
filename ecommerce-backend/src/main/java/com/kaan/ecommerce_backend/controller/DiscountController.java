package com.kaan.ecommerce_backend.controller;

import com.kaan.ecommerce_backend.dto.CouponResponseDto;
import com.kaan.ecommerce_backend.dto.DiscountApplyResponseDto;
import com.kaan.ecommerce_backend.entity.Cart;
import com.kaan.ecommerce_backend.entity.User;
import com.kaan.ecommerce_backend.repository.CartRepository;
import com.kaan.ecommerce_backend.repository.UserRepository;
import com.kaan.ecommerce_backend.service.DiscountService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/discounts")
public class DiscountController {

    private final DiscountService discountService;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;

    public DiscountController(DiscountService discountService, UserRepository userRepository, CartRepository cartRepository) {
        this.discountService = discountService;
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
    }

    @PostMapping("/generate")
    public ResponseEntity<CouponResponseDto> generateCoupon() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();

            String code = discountService.generateFirstOrderCoupon(email);
            return ResponseEntity.ok(new CouponResponseDto("success", code, "Coupon successfully created/retrieved."));
        } catch (Exception e) {
            return ResponseEntity.ok(new CouponResponseDto("fail", null, e.getMessage()));
        }
    }

    @GetMapping("/apply")
    public ResponseEntity<?> applyDiscount(@RequestParam String code) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Cart cart = cartRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("Cart not found"));

            DiscountApplyResponseDto response = discountService.applyDiscount(email, code, cart);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "fail");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }
}