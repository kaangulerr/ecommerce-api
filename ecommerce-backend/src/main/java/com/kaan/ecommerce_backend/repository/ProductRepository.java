package com.kaan.ecommerce_backend.repository;

import com.kaan.ecommerce_backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, String> {

        Optional<Product> findByAsin(String asin);

        @Query("SELECT p FROM Product p WHERE LOWER(CAST(p.title AS string)) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(CAST(p.brandName AS string)) LIKE LOWER(CONCAT('%', :keyword, '%'))")
        List<Product> searchProductsByKeyword(@Param("keyword") String keyword, Pageable pageable);

        @Query("SELECT p FROM Product p WHERE " +
                        "(:mainCategory IS NULL OR p.category = :mainCategory) AND " +
                        "(:subCategory IS NULL OR LOWER(CAST(p.breadcrumbs AS string)) LIKE LOWER(CONCAT('%› %', :subCategory, '%'))) AND "
                        +
                        "(:brand IS NULL OR LOWER(CAST(p.brandName AS string)) LIKE LOWER(CONCAT('%', :brand, '%'))) AND "
                        +
                        "(:color IS NULL OR (LOWER(CAST(p.defaultVariant0 AS string)) LIKE LOWER(CONCAT('%', :color, '%')) OR LOWER(CAST(p.defaultVariant1 AS string)) LIKE LOWER(CONCAT('%', :color, '%')))) AND "
                        +
                        "(:size IS NULL OR (LOWER(CAST(p.defaultVariant0 AS string)) = LOWER(CONCAT('size:', :size)) OR LOWER(CAST(p.defaultVariant1 AS string)) = LOWER(CONCAT('size:', :size)))) AND "
                        +
                        "(:minRating IS NULL OR SUBSTRING(CAST(p.ratingStars AS string), 1, 3) >= :minRating)")
        List<Product> filterProducts(
                        @Param("mainCategory") String mainCategory,
                        @Param("subCategory") String subCategory,
                        @Param("brand") String brand,
                        @Param("color") String color,
                        @Param("size") String size,
                        @Param("minRating") String minRating,
                        Pageable pageable);

        @Query("SELECT p FROM Product p JOIN p.owner o WHERE o.id = :ownerId")
        List<Product> findByOwnerId(@Param("ownerId") Long ownerId, Pageable pageable);

        @Query("SELECT p FROM Product p LEFT JOIN FETCH p.owner")
        List<Product> findAllWithOwner(Pageable pageable);

        @Query("SELECT p FROM Product p JOIN p.owner o WHERE o.id = :sellerId")
        List<Product> findAllBySellerId(@Param("sellerId") Long sellerId, Pageable pageable);

        @Query("SELECT p FROM Product p JOIN p.owner o WHERE LOWER(o.firstName) LIKE LOWER(CONCAT('%', :sellerName, '%')) OR LOWER(o.lastName) LIKE LOWER(CONCAT('%', :sellerName, '%'))")
        List<Product> findByOwnerNameContaining(@Param("sellerName") String sellerName, Pageable pageable);

        @Query(value = "SELECT COUNT(*) FROM products WHERE owner_id = :ownerId", nativeQuery = true)
        Long countActiveProductsByOwnerId(@Param("ownerId") Long ownerId);

        @Query("SELECT new com.kaan.ecommerce_backend.dto.DashboardStatsDto$CategoryCount(p.category, COUNT(p)) " +
                        "FROM Product p GROUP BY p.category ORDER BY COUNT(p) DESC")
        List<com.kaan.ecommerce_backend.dto.DashboardStatsDto.CategoryCount> getCategoryCounts(Pageable pageable);

        List<Product> findTop5ByStockLessThanOrderByStockAsc(int stockLimit);
}