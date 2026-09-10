package com.kaan.ecommerce_backend.dto;

import com.kaan.ecommerce_backend.entity.OrderStatus;
import lombok.Data;

@Data
public class UpdateStatusRequest {
    private OrderStatus status;
}
