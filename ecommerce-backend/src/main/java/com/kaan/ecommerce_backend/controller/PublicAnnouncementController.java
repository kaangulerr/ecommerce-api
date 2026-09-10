package com.kaan.ecommerce_backend.controller;

import com.kaan.ecommerce_backend.entity.SystemAnnouncement;
import com.kaan.ecommerce_backend.service.SystemAnnouncementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/announcements")
public class PublicAnnouncementController {

    private final SystemAnnouncementService announcementService;

    public PublicAnnouncementController(SystemAnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @GetMapping
    public ResponseEntity<List<SystemAnnouncement>> getActiveAnnouncements() {
        return ResponseEntity.ok(announcementService.getActiveAnnouncements());
    }
}
