package com.kaan.ecommerce_backend.dto;

import lombok.Data;
import java.util.List;

/**
 * Satıcının mevcut bir ürünü güncellemesi için kullanılan DTO.
 * PUT /api/seller/products/{id} endpoint'ine gönderilir.
 */
@Data
public class ProductUpdateDto {
    private String title;
    private String aboutItem;
    private String priceValue;
    private String availability;
    private Integer stock;

    private String category;
    private String brandName;
    private List<String> imageUrls;
}
