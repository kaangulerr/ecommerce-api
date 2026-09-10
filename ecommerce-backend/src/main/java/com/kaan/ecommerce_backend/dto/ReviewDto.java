package com.kaan.ecommerce_backend.dto;

import lombok.Data;

@Data
public class ReviewDto {
    private Long id;
    private String reviewTitle;
    private String reviewText;
    private Integer rating;
    private String verifiedPurchase;
    private String reviewMetadata;

    private String userName;

    private boolean createdByCurrentUser;
}