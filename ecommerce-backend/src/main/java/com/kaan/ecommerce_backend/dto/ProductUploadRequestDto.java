package com.kaan.ecommerce_backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * Satıcının ürün yüklemesi için kullanılan DTO.
 * POST /api/products/seller/upload endpoint'ine gönderilir.
 */
@Data
public class ProductUploadRequestDto {

    @NotBlank(message = "Product title cannot be empty")
    private String title;

    @NotBlank(message = "Brand name cannot be empty")
    private String brandName;

    @NotBlank(message = "Price information cannot be empty")
    private String priceValue;

    private String aboutItem;

    private String category;

    private String availability;
    private Integer stock;


    private String deliveryDate;

    private String fastestDeliveryDate;

    /** Ürün görselleri URL listesi (opsiyonel) */
    private List<String> imageUrls;
}
