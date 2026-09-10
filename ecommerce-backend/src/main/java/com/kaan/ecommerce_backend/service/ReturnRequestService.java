package com.kaan.ecommerce_backend.service;

import com.kaan.ecommerce_backend.dto.ResolveReturnRequestDto;
import com.kaan.ecommerce_backend.dto.ReturnRequestCreateDto;
import com.kaan.ecommerce_backend.dto.ReturnRequestDto;
import com.kaan.ecommerce_backend.entity.Order;
import com.kaan.ecommerce_backend.entity.ReturnRequest;
import com.kaan.ecommerce_backend.entity.User;
import com.kaan.ecommerce_backend.repository.OrderRepository;
import com.kaan.ecommerce_backend.repository.ReturnRequestRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReturnRequestService {

    private final ReturnRequestRepository returnRequestRepository;
    private final OrderRepository orderRepository;

    public ReturnRequestService(ReturnRequestRepository returnRequestRepository, OrderRepository orderRepository) {
        this.returnRequestRepository = returnRequestRepository;
        this.orderRepository = orderRepository;
    }

    public ReturnRequestDto createReturnRequest(User user, Long orderId, ReturnRequestCreateDto dto) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!user.getId().equals(order.getUser().getId())) {
            throw new RuntimeException("This order does not belong to you");
        }

        if (!"DELIVERED".equals(order.getStatus())) {
            throw new RuntimeException("Return requests can only be created for DELIVERED orders");
        }

        if (order.getOrderDate() != null && order.getOrderDate().plusDays(14).isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Return requests cannot be created as more than 14 days have passed since the order date");
        }

        if (returnRequestRepository.findByOrderId(orderId).isPresent()) {
            throw new RuntimeException("A return request already exists for this order");
        }

        ReturnRequest request = new ReturnRequest();
        request.setOrder(order);
        request.setUser(user);
        request.setReason(dto.getReason());
        request.setStatus("PENDING");
        request.setRequestDate(LocalDateTime.now());

        ReturnRequest saved = returnRequestRepository.save(request);
        return mapToDto(saved);
    }

    public List<ReturnRequestDto> getMyReturnRequests(User user) {
        return returnRequestRepository.findByUser(user).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<ReturnRequestDto> getSellerReturnRequests(Long sellerId) {
        return returnRequestRepository.findBySellerId(sellerId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public ReturnRequestDto resolveReturnRequest(Long id, ResolveReturnRequestDto dto, Long sellerId) {
        ReturnRequest request = returnRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Return request not found"));

        boolean hasSellerProduct = request.getOrder().getItems().stream()
                .anyMatch(item -> item.getProduct().getOwner() != null && item.getProduct().getOwner().getId().equals(sellerId));

        if (!hasSellerProduct) {
            throw new RuntimeException("You are not authorized to manage this return request");
        }

        request.setStatus(dto.getStatus());
        request.setSellerNote(dto.getSellerNote());
        request.setResolvedDate(LocalDateTime.now());

        if ("APPROVED".equals(dto.getStatus())) {
            Order order = request.getOrder();
            order.setStatus("CANCELLED");
            orderRepository.save(order);
        }

        ReturnRequest saved = returnRequestRepository.save(request);
        return mapToDto(saved);
    }

    private ReturnRequestDto mapToDto(ReturnRequest entity) {
        ReturnRequestDto dto = new ReturnRequestDto();
        dto.setId(entity.getId());
        dto.setOrderId(entity.getOrder().getId());
        dto.setUserId(entity.getUser().getId());
        dto.setReason(entity.getReason());
        dto.setStatus(entity.getStatus());
        dto.setRequestDate(entity.getRequestDate());
        dto.setResolvedDate(entity.getResolvedDate());
        dto.setSellerNote(entity.getSellerNote());
        return dto;
    }
}
