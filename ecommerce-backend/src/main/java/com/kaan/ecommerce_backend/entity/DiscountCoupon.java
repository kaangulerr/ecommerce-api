package com.kaan.ecommerce_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "discount_coupons")
public class DiscountCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String couponCode;

    @Column(nullable = false)
    private String userEmail;

    private boolean isUsed = false;

    private double discountPercentage = 15.0;
}