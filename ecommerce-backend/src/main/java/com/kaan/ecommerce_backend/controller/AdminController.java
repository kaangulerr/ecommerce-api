package com.kaan.ecommerce_backend.controller;

import com.kaan.ecommerce_backend.dto.AdminProductDto;
import com.kaan.ecommerce_backend.dto.DashboardStatsDto;

import com.kaan.ecommerce_backend.entity.ContactMessage;
import com.kaan.ecommerce_backend.entity.Order;
import com.kaan.ecommerce_backend.entity.SystemAnnouncement;
import com.kaan.ecommerce_backend.dto.SystemAnnouncementRequestDto;
import com.kaan.ecommerce_backend.service.*;
import com.kaan.ecommerce_backend.dto.UserResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminController {

    private final OrderService orderService;
    private final ProductService productService;
    private final ContactMessageService contactMessageService;
    private final UserService userService;
    private final SystemAnnouncementService systemAnnouncementService;
    private final DashboardService dashboardService;

    public AdminController(OrderService orderService, ProductService productService, ContactMessageService contactMessageService, UserService userService, SystemAnnouncementService systemAnnouncementService, DashboardService dashboardService) {
        this.orderService = orderService;
        this.productService = productService;
        this.contactMessageService = contactMessageService;
        this.userService = userService;
        this.systemAnnouncementService = systemAnnouncementService;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStatsDto> getDashboardStats() {
        return ResponseEntity.ok(dashboardService.getDashboardStats());
    }


    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getAllOrdersForAdmin(
            @RequestParam(value = "sellerName", required = false) String sellerName) {
        return ResponseEntity.ok(orderService.getAllOrdersForAdmin(sellerName));
    }


    /**
     * GET /api/admin/products
     * Admin dashboard: Tüm ürünleri satıcı bilgisiyle listeler.
     * Opsiyonel parametreler:
     *   - sellerId: Belirli satıcının ürünlerini filtreler
     *   - sellerName: Satıcı ismine göre esnek arama (firstName veya lastName eşleşmesi, büyük/küçük harf duyarsız)
     */
    @GetMapping("/products")
    public ResponseEntity<List<AdminProductDto>> getAllProductsForAdmin(
            @RequestParam(value = "sellerId", required = false) Long sellerId,
            @RequestParam(value = "sellerName", required = false) String sellerName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(productService.getAllProductsForAdmin(sellerId, sellerName, page, size));
    }

    @GetMapping("/contact")
    public ResponseEntity<List<ContactMessage>> getContactMessages(
            @RequestParam(defaultValue = "false") boolean unreadOnly) {
        return ResponseEntity.ok(contactMessageService.getAllMessages(unreadOnly));
    }

    @PatchMapping("/contact/{id}/read")
    public ResponseEntity<ContactMessage> markContactMessageAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(contactMessageService.markAsRead(id));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDto>> getAllUsers(
            @RequestParam(required = false) String role) {
        return ResponseEntity.ok(userService.getAllUsersForAdmin(role));
    }

    @PostMapping("/announcements")
    public ResponseEntity<SystemAnnouncement> createAnnouncement(@RequestBody SystemAnnouncementRequestDto requestDto) {
        return ResponseEntity.ok(systemAnnouncementService.createAnnouncement(requestDto));
    }

    @PatchMapping("/announcements/{id}/toggle")
    public ResponseEntity<SystemAnnouncement> toggleAnnouncement(@PathVariable Long id) {
        return ResponseEntity.ok(systemAnnouncementService.toggleAnnouncement(id));
    }

    @GetMapping("/announcements")
    public ResponseEntity<List<SystemAnnouncement>> getAllAnnouncements() {
        return ResponseEntity.ok(systemAnnouncementService.getAllAnnouncements());
    }
}