package com.kaan.ecommerce_backend.repository;

import com.kaan.ecommerce_backend.entity.Favorite;
import com.kaan.ecommerce_backend.entity.Product;
import com.kaan.ecommerce_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    List<Favorite> findByUser(User user);

    Optional<Favorite> findByUserAndProduct(User user, Product product);

    void deleteByUserAndProduct(User user, Product product);

    @Query("SELECT COUNT(f) > 0 FROM Favorite f WHERE f.user.id = :userId AND TRIM(f.product.asin) = TRIM(:asin)")
    boolean checkFavorite(@Param("userId") Long userId, @Param("asin") String asin);
}