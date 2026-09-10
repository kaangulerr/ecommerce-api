package com.kaan.ecommerce_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name = "products")
public class Product {
    @Id
    private String asin;

    @Column(columnDefinition = "TEXT") private String sNo;
    @Column(columnDefinition = "LONGTEXT") private String aboutItem;
    @Column(columnDefinition = "TEXT") private String availability;
    @Column(columnDefinition = "TEXT") private String bestSellersRank;
    @Column(columnDefinition = "TEXT") private String brandName;
    @Column(columnDefinition = "TEXT") private String brandPageUrl;
    @Column(columnDefinition = "LONGTEXT") private String breadcrumbs;
    @Column(columnDefinition = "LONGTEXT") private String customerReviewSummary;

    @Column(name = "default_variant0", columnDefinition = "TEXT")
    private String defaultVariant0;

    @Column(name = "default_variant1", columnDefinition = "TEXT")
    private String defaultVariant1;

    @Column(columnDefinition = "TEXT") private String deliveryDate;
    @Column(columnDefinition = "TEXT") private String fastestDeliveryDate;
    @Column(columnDefinition = "TEXT") private String priceValue;
    @Column(columnDefinition = "TEXT") private String productUrl;
    @Column(columnDefinition = "TEXT") private String ratingCount;

    @Column(columnDefinition = "TEXT") private String ratingDistribution1star;
    @Column(columnDefinition = "TEXT") private String ratingDistribution2star;
    @Column(columnDefinition = "TEXT") private String ratingDistribution3star;
    @Column(columnDefinition = "TEXT") private String ratingDistribution4star;
    @Column(columnDefinition = "TEXT") private String ratingDistribution5star;

    @Column(columnDefinition = "TEXT") private String ratingStars;
    @Column(columnDefinition = "TEXT") private String recentPurchases;
    @Column(columnDefinition = "TEXT") private String scrapeTime;
    @Column(columnDefinition = "TEXT") private String sellerName;
    @Column(columnDefinition = "LONGTEXT") private String title;
    @Column(columnDefinition = "LONGTEXT") private String allImages;
    @Column(columnDefinition = "TEXT") private String rank1;

    private String category;
    private Integer stock = 0;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    @JsonIgnore
    private User owner;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductImage> images;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductReview> reviews;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Favorite> favorites;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<CartItem> cartItems;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<OrderItem> orderItems;
}


