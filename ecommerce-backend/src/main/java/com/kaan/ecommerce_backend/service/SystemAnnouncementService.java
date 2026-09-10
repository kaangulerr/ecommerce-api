package com.kaan.ecommerce_backend.service;

import com.kaan.ecommerce_backend.dto.SystemAnnouncementRequestDto;
import com.kaan.ecommerce_backend.entity.SystemAnnouncement;
import com.kaan.ecommerce_backend.repository.SystemAnnouncementRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SystemAnnouncementService {

    private final SystemAnnouncementRepository announcementRepository;

    public SystemAnnouncementService(SystemAnnouncementRepository announcementRepository) {
        this.announcementRepository = announcementRepository;
    }

    public SystemAnnouncement createAnnouncement(SystemAnnouncementRequestDto requestDto) {
        SystemAnnouncement announcement = new SystemAnnouncement();
        announcement.setMessage(requestDto.getMessage());
        announcement.setActive(requestDto.isActive());
        return announcementRepository.save(announcement);
    }

    public SystemAnnouncement toggleAnnouncement(Long id) {
        SystemAnnouncement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Announcement not found with id: " + id));
        announcement.setActive(!announcement.isActive());
        return announcementRepository.save(announcement);
    }

    public List<SystemAnnouncement> getAllAnnouncements() {
        return announcementRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<SystemAnnouncement> getActiveAnnouncements() {
        return announcementRepository.findByIsActiveTrueOrderByCreatedAtDesc();
    }
}
