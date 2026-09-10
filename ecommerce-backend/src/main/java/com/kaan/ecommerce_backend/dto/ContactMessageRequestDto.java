package com.kaan.ecommerce_backend.dto;

import lombok.Data;

@Data
public class ContactMessageRequestDto {
    private String fullName;
    private String email;
    private String subject;
    private String message;
}
