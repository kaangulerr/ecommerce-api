package com.kaan.ecommerce_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "product_reviews")
public class ProductReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String sNo;
    
    @Column(name = "helpful_count", columnDefinition = "TEXT")
    private String helpfulvotecount;
    
    @Column(length = 20)
    private String productasin;
    private Integer rating;
    @Column(length = 50)
    private String reviewid;
    @Column(columnDefinition = "LONGTEXT")
    private String reviewmetadata;
    @Column(columnDefinition = "TEXT")
    private String reviewposition;
    @Column(columnDefinition = "LONGTEXT")
    private String reviewtext;
    @Column(columnDefinition = "LONGTEXT")
    private String reviewtitle;
    @Column(columnDefinition = "LONGTEXT")
    private String reviewurl;
    @Column(length = 10)
    private String verifiedpurchase;
    @Column(columnDefinition = "LONGTEXT")
    private String cleanedReviewText;
    @Column(length = 20)
    private String sentimentScore;

    @ManyToOne
    @JoinColumn(name = "product_id")
    @JsonIgnore
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;
}