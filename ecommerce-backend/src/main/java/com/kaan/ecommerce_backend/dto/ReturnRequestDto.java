package com.kaan.ecommerce_backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReturnRequestDto {
    private Long id;
    private Long orderId;
    private Long userId;
    private String reason;
    private String status;
    private LocalDateTime requestDate;
    private LocalDateTime resolvedDate;
    private String sellerNote;
}
