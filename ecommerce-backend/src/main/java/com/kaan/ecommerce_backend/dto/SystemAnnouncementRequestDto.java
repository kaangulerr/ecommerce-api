package com.kaan.ecommerce_backend.dto;

import lombok.Data;

@Data
public class SystemAnnouncementRequestDto {
    private String message;
    private boolean isActive = true;
}
