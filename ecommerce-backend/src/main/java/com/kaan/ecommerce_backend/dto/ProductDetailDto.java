package com.kaan.ecommerce_backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class ProductDetailDto {
    private String asin;
    private String title;
    private String priceValue;
    private String brandName;
    private String ratingStars;
    private String ratingCount;
    private List<String> allImages;

    private String aboutItem;
    private String availability;
    private String breadcrumbs;
    private String customerReviewSummary;
    private String deliveryDate;
    private String fastestDeliveryDate;
    private String sellerName;

    private String ratingDistribution1star;
    private String ratingDistribution2star;
    private String ratingDistribution3star;
    private String ratingDistribution4star;
    private String ratingDistribution5star;

    private boolean isFavorite;
}