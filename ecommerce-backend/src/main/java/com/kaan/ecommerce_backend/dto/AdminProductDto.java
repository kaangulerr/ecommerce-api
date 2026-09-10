package com.kaan.ecommerce_backend.dto;

import lombok.Data;

import java.util.List;

/**
 * Admin dashboard'da ürün listelerken kullanılan DTO.
 * Satıcı bilgileriyle zenginleştirilmiştir.
 */
@Data
public class AdminProductDto {

    private String asin;
    private String title;
    private String brandName;
    private String priceValue;
    private String category;
    private String ratingStars;
    private String ratingCount;
    private List<String> allImages;

    private Long ownerId;
    private String ownerEmail;
    private String ownerFullName;
}
