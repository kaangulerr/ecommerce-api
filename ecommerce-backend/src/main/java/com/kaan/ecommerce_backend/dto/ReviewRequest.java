package com.kaan.ecommerce_backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReviewRequest {
    @NotBlank(message = "Title cannot be empty")
    private String reviewTitle;

    @NotBlank(message = "Review content cannot be empty")
    @Size(min = 10, message = "Review must be at least 10 characters")
    private String reviewText;

    @NotNull(message = "Rating cannot be empty")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot be more than 5")
    private Integer rating;
}
