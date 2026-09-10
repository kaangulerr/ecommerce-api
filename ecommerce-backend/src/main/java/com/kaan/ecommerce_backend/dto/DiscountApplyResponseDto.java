package com.kaan.ecommerce_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiscountApplyResponseDto {
    private String status;
    private double originalTotal;
    private double discountedTotal;
    private List<DiscountItemDto> items;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DiscountItemDto {
        private String productId;
        private double originalPrice;
        private double discountedPrice;
    }
}
