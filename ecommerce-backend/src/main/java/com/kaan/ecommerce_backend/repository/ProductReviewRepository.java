package com.kaan.ecommerce_backend.repository;

import com.kaan.ecommerce_backend.entity.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {

    List<ProductReview> findByProduct_Asin(String asin);
}