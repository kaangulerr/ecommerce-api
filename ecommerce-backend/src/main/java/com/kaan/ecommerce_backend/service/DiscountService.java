package com.kaan.ecommerce_backend.service;

import com.kaan.ecommerce_backend.dto.DiscountApplyResponseDto;
import com.kaan.ecommerce_backend.entity.Cart;
import com.kaan.ecommerce_backend.entity.CartItem;
import com.kaan.ecommerce_backend.entity.DiscountCoupon;
import com.kaan.ecommerce_backend.repository.CartItemRepository;
import com.kaan.ecommerce_backend.repository.CartRepository;
import com.kaan.ecommerce_backend.repository.DiscountCouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DiscountService {

    private final DiscountCouponRepository discountCouponRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public DiscountService(DiscountCouponRepository discountCouponRepository, 
                           CartRepository cartRepository, 
                           CartItemRepository cartItemRepository) {
        this.discountCouponRepository = discountCouponRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    public String generateFirstOrderCoupon(String email) {
        Optional<DiscountCoupon> existingCoupon = discountCouponRepository.findByUserEmail(email);
        if (existingCoupon.isPresent()) {
            DiscountCoupon coupon = existingCoupon.get();
            if (coupon.isUsed()) {
                throw new RuntimeException("You have already used a coupon!");
            }
            return coupon.getCouponCode();
        }

        String prefix = email.split("@")[0].toUpperCase();
        String randomSuffix = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        String generatedCode = prefix + "-WELCOME-" + randomSuffix;

        DiscountCoupon newCoupon = new DiscountCoupon();
        newCoupon.setCouponCode(generatedCode);
        newCoupon.setUserEmail(email);
        newCoupon.setUsed(false);
        newCoupon.setDiscountPercentage(15.0);

        discountCouponRepository.save(newCoupon);

        return generatedCode;
    }

    @Transactional
    public DiscountApplyResponseDto applyDiscount(String email, String couponCode, Cart cart) {
        DiscountCoupon coupon = discountCouponRepository.findByCouponCode(couponCode)
                .orElseThrow(() -> new RuntimeException("Invalid or incorrect discount code!"));

        if (!coupon.getUserEmail().equals(email)) {
            throw new RuntimeException("This discount code does not belong to your account!");
        }

        if (coupon.isUsed()) {
            throw new RuntimeException("You have already used this discount code in a previous order!");
        }

        double originalTotal = cart.getOriginalTotalPrice();
        List<DiscountApplyResponseDto.DiscountItemDto> itemDtos = new ArrayList<>();

        for (CartItem item : cart.getItems()) {
            if (item.getProduct() != null && item.getProduct().getPriceValue() != null) {
                try {
                    double originalPrice = Double.parseDouble(item.getProduct().getPriceValue());
                    double rawDiscountedPrice = originalPrice * (1 - (coupon.getDiscountPercentage() / 100));
                    
                    double discountedPrice = BigDecimal.valueOf(rawDiscountedPrice)
                            .setScale(2, RoundingMode.HALF_UP)
                            .doubleValue();
                    
                    item.setDiscountedPrice(discountedPrice);
                    cartItemRepository.save(item);

                    itemDtos.add(new DiscountApplyResponseDto.DiscountItemDto(
                            item.getProduct().getAsin(),
                            originalPrice,
                            discountedPrice
                    ));
                } catch (NumberFormatException ignored) {}
            }
        }

        cart.setAppliedCouponCode(couponCode);
        cartRepository.save(cart);

        double discountedTotal = cart.getTotalPrice();

        return new DiscountApplyResponseDto(
                "success",
                originalTotal,
                discountedTotal,
                itemDtos
        );
    }

    public void markCouponAsUsed(String couponCode) {
        Optional<DiscountCoupon> couponOpt = discountCouponRepository.findByCouponCode(couponCode);
        if (couponOpt.isPresent()) {
            DiscountCoupon coupon = couponOpt.get();
            coupon.setUsed(true);
            discountCouponRepository.save(coupon);
        }
    }
}