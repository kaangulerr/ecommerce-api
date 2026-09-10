package com.kaan.ecommerce_backend.service;

import com.kaan.ecommerce_backend.dto.ContactMessageRequestDto;
import com.kaan.ecommerce_backend.entity.ContactMessage;
import com.kaan.ecommerce_backend.repository.ContactMessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContactMessageService {

    private final ContactMessageRepository contactMessageRepository;

    public ContactMessageService(ContactMessageRepository contactMessageRepository) {
        this.contactMessageRepository = contactMessageRepository;
    }

    public ContactMessage createMessage(ContactMessageRequestDto requestDto) {
        ContactMessage message = new ContactMessage();
        message.setFullName(requestDto.getFullName());
        message.setEmail(requestDto.getEmail());
        message.setSubject(requestDto.getSubject());
        message.setMessage(requestDto.getMessage());
        return contactMessageRepository.save(message);
    }

    public List<ContactMessage> getAllMessages(boolean unreadOnly) {
        if (unreadOnly) {
            return contactMessageRepository.findByIsReadFalse();
        }
        return contactMessageRepository.findAll();
    }

    public ContactMessage markAsRead(Long id) {
        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contact message not found with id: " + id));
        message.setRead(true);
        return contactMessageRepository.save(message);
    }
}
