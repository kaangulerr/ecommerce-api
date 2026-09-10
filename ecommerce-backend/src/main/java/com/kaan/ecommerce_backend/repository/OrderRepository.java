package com.kaan.ecommerce_backend.repository;

import com.kaan.ecommerce_backend.entity.Order;
import com.kaan.ecommerce_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserOrderByOrderDateDesc(User user);

    List<Order> findAllByOrderByOrderDateDesc();

    @Query("SELECT DISTINCT o FROM Order o JOIN o.items i JOIN i.product p JOIN p.owner s " +
           "WHERE LOWER(s.firstName) LIKE LOWER(CONCAT('%', :sellerName, '%')) " +
           "OR LOWER(s.lastName) LIKE LOWER(CONCAT('%', :sellerName, '%')) " +
           "ORDER BY o.orderDate DESC")
    List<Order> findBySellerName(@Param("sellerName") String sellerName);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.items i JOIN i.product p WHERE p.owner.id = :sellerId ORDER BY o.orderDate DESC")
    List<Order> findBySellerId(@Param("sellerId") Long sellerId);

    @Query("SELECT SUM(i.quantity) FROM Order o JOIN o.items i JOIN i.product p WHERE p.owner.id = :sellerId AND o.status != 'CANCELLED'")
    Long countItemsBySeller(@Param("sellerId") Long sellerId);

    @Query("SELECT SUM(i.priceAtPurchase * i.quantity) FROM Order o JOIN o.items i JOIN i.product p WHERE p.owner.id = :sellerId AND o.status != 'CANCELLED'")
    Double sumRevenueBySeller(@Param("sellerId") Long sellerId);

    @Query("SELECT COUNT(DISTINCT o) FROM Order o JOIN o.items i JOIN i.product p WHERE p.owner.id = :sellerId AND o.status = 'PENDING'")
    Long countPendingOrdersBySeller(@Param("sellerId") Long sellerId);

    @Query("SELECT COUNT(DISTINCT o) FROM Order o JOIN o.items i JOIN i.product p WHERE p.owner.id = :sellerId AND o.status = 'SHIPPED'")
    Long countShippedOrdersBySeller(@Param("sellerId") Long sellerId);

    @Query("SELECT COUNT(DISTINCT o) FROM Order o JOIN o.items i JOIN i.product p WHERE p.owner.id = :sellerId AND o.status = 'DELIVERED'")
    Long countDeliveredOrdersBySeller(@Param("sellerId") Long sellerId);

    @Query("SELECT COUNT(DISTINCT o) FROM Order o JOIN o.items i JOIN i.product p WHERE p.owner.id = :sellerId AND o.status = 'CANCELLED'")
    Long countCancelledOrdersBySeller(@Param("sellerId") Long sellerId);

    @Query("SELECT SUM(o.totalAmount) FROM Order o")
    Double sumTotalAmount();

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.orderDate >= :since")
    Double sumTotalAmountSince(@Param("since") java.time.LocalDateTime since);

    List<Order> findTop5ByOrderByOrderDateDesc();

    @Query("SELECT o FROM Order o WHERE o.orderDate >= :since")
    List<Order> findAllSince(@Param("since") java.time.LocalDateTime since);

    @Query("SELECT CAST(o.orderDate AS LocalDate) as d, SUM(i.priceAtPurchase * i.quantity) " +
           "FROM Order o JOIN o.items i JOIN i.product p " +
           "WHERE p.owner.id = :sellerId AND o.status != 'CANCELLED' " +
           "GROUP BY d ORDER BY d ASC")
    List<Object[]> getDailyRevenueBySeller(@Param("sellerId") Long sellerId);

    @Query("SELECT p.category, SUM(i.quantity) " +
           "FROM Order o JOIN o.items i JOIN i.product p " +
           "WHERE p.owner.id = :sellerId AND o.status != 'CANCELLED' " +
           "GROUP BY p.category")
    List<Object[]> getCategoryDistributionBySeller(@Param("sellerId") Long sellerId);
}