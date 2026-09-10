package com.kaan.ecommerce_backend.repository;

import com.kaan.ecommerce_backend.entity.ReturnRequest;
import com.kaan.ecommerce_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {

    List<ReturnRequest> findByUser(User user);

    @Query("SELECT r FROM ReturnRequest r JOIN r.order o JOIN o.items i JOIN i.product p " +
            "WHERE p.owner.id = :sellerId ORDER BY r.requestDate DESC")
    List<ReturnRequest> findBySellerId(@Param("sellerId") Long sellerId);

    Optional<ReturnRequest> findByOrderId(Long orderId);
}
