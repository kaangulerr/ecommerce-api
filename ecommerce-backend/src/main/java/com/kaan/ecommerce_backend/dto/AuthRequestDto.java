package com.kaan.ecommerce_backend.dto;

import lombok.Data;

@Data
public class AuthRequestDto {
    private String email;
    private String firstName;
    private String lastName;
    private String authProvider;
}