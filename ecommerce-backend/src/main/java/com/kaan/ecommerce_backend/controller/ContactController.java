package com.kaan.ecommerce_backend.controller;

import com.kaan.ecommerce_backend.dto.ContactMessageRequestDto;
import com.kaan.ecommerce_backend.entity.ContactMessage;
import com.kaan.ecommerce_backend.service.ContactMessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
public class ContactController {

    private final ContactMessageService contactMessageService;

    public ContactController(ContactMessageService contactMessageService) {
        this.contactMessageService = contactMessageService;
    }

    @PostMapping
    public ResponseEntity<ContactMessage> sendMessage(@RequestBody ContactMessageRequestDto requestDto) {
        return ResponseEntity.ok(contactMessageService.createMessage(requestDto));
    }
}
