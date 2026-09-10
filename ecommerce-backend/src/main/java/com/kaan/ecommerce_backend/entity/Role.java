package com.kaan.ecommerce_backend.entity;

/**
 * Sistemdeki kullanıcı rollerini tanımlar.
 * USER  – Standart kullanıcı (alışveriş yapabilir)
 * ADMIN – Yönetici (tüm sistemi yönetebilir)
 * SELLER – Satıcı (ürün yükleyebilir ve kendi ürünlerini yönetebilir)
 */
public enum Role {
    USER,
    ADMIN,
    SELLER
}
