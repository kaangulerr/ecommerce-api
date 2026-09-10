package com.kaan.ecommerce_backend.service;

import com.kaan.ecommerce_backend.entity.*;
import com.kaan.ecommerce_backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Cart addToCart(User user, String asin, int quantity) {
        Product product = productRepository.findByAsin(asin)
                .orElseThrow(() -> new RuntimeException("Product not found!"));

        Cart cart = cartRepository.findByUser(user).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepository.save(newCart);
        });

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getAsin().equals(asin))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            cart.getItems().add(newItem);
        }

        return cartRepository.save(cart);
    }

    @Transactional
    public Cart removeItemFromCart(User user, String asin) {
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("User cart not found!"));

        cart.getItems().removeIf(item -> item.getProduct().getAsin().equals(asin));

        return cartRepository.save(cart);
    }

    @Transactional
    public Cart updateItemQuantity(User user, String asin, int newQuantity) {
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("User cart not found!"));

        if (newQuantity <= 0) {
            return removeItemFromCart(user, asin);
        }

        Optional<CartItem> itemToUpdate = cart.getItems().stream()
                .filter(item -> item.getProduct().getAsin().equals(asin))
                .findFirst();

        if (itemToUpdate.isPresent()) {
            itemToUpdate.get().setQuantity(newQuantity);
            return cartRepository.save(cart);
        } else {
            throw new RuntimeException("This product was not found in your cart!");
        }
    }
}