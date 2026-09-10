package com.kaan.ecommerce_backend.repository;

import com.kaan.ecommerce_backend.entity.DiscountCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DiscountCouponRepository extends JpaRepository<DiscountCoupon, Long> {

    Optional<DiscountCoupon> findByCouponCode(String couponCode);

    Optional<DiscountCoupon> findByUserEmail(String userEmail);
}