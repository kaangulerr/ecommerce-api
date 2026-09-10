package com.kaan.ecommerce_backend.controller;

import com.kaan.ecommerce_backend.entity.Cart;
import com.kaan.ecommerce_backend.entity.User;
import com.kaan.ecommerce_backend.repository.UserRepository;
import com.kaan.ecommerce_backend.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    public CartController(CartService cartService, UserRepository userRepository) {
        this.cartService = cartService;
        this.userRepository = userRepository;
    }

    @PostMapping("/add")
    public ResponseEntity<Cart> addToCart(@RequestParam String asin, @RequestParam int quantity) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        return ResponseEntity.ok(cartService.addToCart(user, asin, quantity));
    }

    @GetMapping
    public ResponseEntity<Cart> getMyCart() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        Cart cart = user.getCart();

        if (cart == null) {
            cart = new Cart();
        }

        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/remove/{asin}")
    public ResponseEntity<Cart> removeItemFromCart(@PathVariable String asin) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        return ResponseEntity.ok(cartService.removeItemFromCart(user, asin));
    }

    @PutMapping("/update/{asin}")
    public ResponseEntity<Cart> updateQuantity(@PathVariable String asin, @RequestParam int quantity) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        return ResponseEntity.ok(cartService.updateItemQuantity(user, asin, quantity));
    }
}