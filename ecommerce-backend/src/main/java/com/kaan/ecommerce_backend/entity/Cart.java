package com.kaan.ecommerce_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    private String appliedCouponCode;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();


    @JsonProperty("totalItems")
    @Transient
    public int getTotalItems() {
        if (items == null) return 0;
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    @JsonProperty("totalPrice")
    @Transient
    public double getTotalPrice() {
        if (items == null) return 0.0;

        double total = 0.0;
        for (CartItem item : items) {
            double priceToUse = 0.0;
            if (item.getDiscountedPrice() != null) {
                priceToUse = item.getDiscountedPrice();
            } else if (item.getProduct() != null && item.getProduct().getPriceValue() != null) {
                try {
                    priceToUse = Double.parseDouble(item.getProduct().getPriceValue());
                } catch (NumberFormatException ignored) {}
            }
            total += priceToUse * item.getQuantity();
        }
        return BigDecimal.valueOf(total).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    @JsonProperty("originalTotalPrice")
    @Transient
    public double getOriginalTotalPrice() {
        if (items == null) return 0.0;

        double total = 0.0;
        for (CartItem item : items) {
            if (item.getProduct() != null && item.getProduct().getPriceValue() != null) {
                try {
                    double price = Double.parseDouble(item.getProduct().getPriceValue());
                    total += price * item.getQuantity();
                } catch (NumberFormatException ignored) {}
            }
        }
        return BigDecimal.valueOf(total).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}