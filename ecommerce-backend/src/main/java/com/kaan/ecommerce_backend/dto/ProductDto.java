package com.kaan.ecommerce_backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class ProductDto {
    private String asin;
    private String title;
    private String priceValue;
    private String brandName;
    private String ratingStars;
    private String ratingCount;
    private List<String> allImages;
    private boolean isFavorite;
    private String defaultVariant0;
    private String defaultVariant1;
}