package com.kaan.ecommerce_backend.repository;

import com.kaan.ecommerce_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    List<User> findByRoleOrderByIdAsc(String role);
    
    List<User> findAllByOrderByIdAsc();

    long countByRole(String role);
}
