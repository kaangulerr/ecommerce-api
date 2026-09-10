package com.kaan.ecommerce_backend.dto;

import lombok.Data;

@Data
public class FavoriteDto {
    private Long favoriteId;
    private String productAsin;
    private String title;
    private String priceValue;
    private String imageUrl;
}