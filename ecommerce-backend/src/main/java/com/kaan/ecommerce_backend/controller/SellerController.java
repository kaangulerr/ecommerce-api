package com.kaan.ecommerce_backend.controller;

import com.kaan.ecommerce_backend.dto.SellerStatsDto;
import com.kaan.ecommerce_backend.dto.ProductDto;
import com.kaan.ecommerce_backend.dto.ProductUploadRequestDto;
import com.kaan.ecommerce_backend.dto.ProductUpdateDto;
import com.kaan.ecommerce_backend.dto.UpdateStatusRequest;
import com.kaan.ecommerce_backend.entity.Order;
import com.kaan.ecommerce_backend.entity.OrderStatus;
import com.kaan.ecommerce_backend.entity.User;
import com.kaan.ecommerce_backend.repository.UserRepository;
import com.kaan.ecommerce_backend.service.SellerService;
import com.kaan.ecommerce_backend.service.ProductService;
import com.kaan.ecommerce_backend.service.ReturnRequestService;
import com.kaan.ecommerce_backend.dto.ResolveReturnRequestDto;
import com.kaan.ecommerce_backend.dto.ReturnRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/seller")
@PreAuthorize("hasAuthority('SELLER')")
public class SellerController {

    private final SellerService sellerService;
    private final ProductService productService;
    private final UserRepository userRepository;
    private final ReturnRequestService returnRequestService;

    public SellerController(SellerService sellerService, ProductService productService, UserRepository userRepository, ReturnRequestService returnRequestService) {
        this.sellerService = sellerService;
        this.productService = productService;
        this.userRepository = userRepository;
        this.returnRequestService = returnRequestService;
    }

    private Long getSellerId(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Seller not found"))
                .getId();
    }


    @PostMapping("/products")
    public ResponseEntity<ProductDto> uploadProduct(
            Authentication authentication,
            @RequestBody ProductUploadRequestDto request) {
        return ResponseEntity.ok(productService.uploadProduct(request, getSellerId(authentication)));
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductDto>> getMyProducts(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(productService.getSellerProducts(getSellerId(authentication), page, size));
    }

    @PutMapping("/products/{asin}")
    public ResponseEntity<ProductDto> updateProduct(
            Authentication authentication,
            @PathVariable String asin,
            @RequestBody ProductUpdateDto dto) {
        return ResponseEntity.ok(productService.updateProduct(asin, dto, getSellerId(authentication)));
    }

    @DeleteMapping("/products/{asin}")
    public ResponseEntity<Void> deleteProduct(
            Authentication authentication,
            @PathVariable String asin) {
        productService.deleteProduct(asin, getSellerId(authentication));
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getSellerOrders(Authentication authentication) {
        return ResponseEntity.ok(sellerService.getSellerOrders(getSellerId(authentication)));
    }

    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<Order> updateOrderStatus(
            Authentication authentication,
            @PathVariable Long orderId,
            @RequestBody UpdateStatusRequest request) {
        
        OrderStatus status = request.getStatus();
        if (status == null) {
            throw new RuntimeException("Status field is required");
        }
        
        return ResponseEntity.ok(sellerService.updateOrderStatus(orderId, status.name(), getSellerId(authentication)));
    }

    @GetMapping("/statistics")
    public ResponseEntity<SellerStatsDto> getSellerStatistics(Authentication authentication) {
        return ResponseEntity.ok(sellerService.getSellerStatistics(getSellerId(authentication)));
    }


    @GetMapping("/returns")
    public ResponseEntity<List<ReturnRequestDto>> getSellerReturnRequests(Authentication authentication) {
        return ResponseEntity.ok(returnRequestService.getSellerReturnRequests(getSellerId(authentication)));
    }

    @PatchMapping("/returns/{id}/resolve")
    public ResponseEntity<ReturnRequestDto> resolveReturnRequest(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody ResolveReturnRequestDto dto) {
        return ResponseEntity.ok(returnRequestService.resolveReturnRequest(id, dto, getSellerId(authentication)));
    }
}
