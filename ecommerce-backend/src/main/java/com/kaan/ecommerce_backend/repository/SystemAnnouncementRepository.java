package com.kaan.ecommerce_backend.repository;

import com.kaan.ecommerce_backend.entity.SystemAnnouncement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SystemAnnouncementRepository extends JpaRepository<SystemAnnouncement, Long> {
    List<SystemAnnouncement> findByIsActiveTrueOrderByCreatedAtDesc();
    List<SystemAnnouncement> findAllByOrderByCreatedAtDesc();
}
