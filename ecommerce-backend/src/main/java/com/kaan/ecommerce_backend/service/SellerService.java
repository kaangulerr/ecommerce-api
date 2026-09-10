package com.kaan.ecommerce_backend.service;

import com.kaan.ecommerce_backend.dto.SellerStatsDto;
import com.kaan.ecommerce_backend.entity.Order;
import com.kaan.ecommerce_backend.repository.OrderRepository;
import com.kaan.ecommerce_backend.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SellerService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public SellerService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    public List<Order> getSellerOrders(Long sellerId) {
        return orderRepository.findBySellerId(sellerId);
    }

    public Order updateOrderStatus(Long orderId, String status, Long sellerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        boolean isSellerInvolved = order.getItems().stream()
                .anyMatch(item -> item.getProduct().getOwner() != null && item.getProduct().getOwner().getId().equals(sellerId));

        if (!isSellerInvolved) {
            throw new RuntimeException("You are not authorized to update this order.");
        }

        order.setStatus(status);
        return orderRepository.save(order);
    }

    public SellerStatsDto getSellerStatistics(Long sellerId) {
        Long totalSalesCount = orderRepository.countItemsBySeller(sellerId);
        Double totalRevenue = orderRepository.sumRevenueBySeller(sellerId);
        Long pendingOrderCount = orderRepository.countPendingOrdersBySeller(sellerId);
        Long shippedOrderCount = orderRepository.countShippedOrdersBySeller(sellerId);
        Long deliveredOrderCount = orderRepository.countDeliveredOrdersBySeller(sellerId);
        Long cancelledOrderCount = orderRepository.countCancelledOrdersBySeller(sellerId);
        Long activeProductsCount = productRepository.countActiveProductsByOwnerId(sellerId);

        List<Object[]> dailyRevenueRaw = orderRepository.getDailyRevenueBySeller(sellerId);
        List<SellerStatsDto.DailyRevenueDto> dailyRevenue = dailyRevenueRaw.stream()
                .map(obj -> new SellerStatsDto.DailyRevenueDto(
                        obj[0].toString(),
                        BigDecimal.valueOf(((Number) obj[1]).doubleValue())
                                .setScale(2, RoundingMode.HALF_UP)
                                .doubleValue()
                ))
                .collect(Collectors.toList());

        List<Object[]> categoryDistributionRaw = orderRepository.getCategoryDistributionBySeller(sellerId);
        List<SellerStatsDto.CategoryDistributionDto> categoryDistribution = categoryDistributionRaw.stream()
                .map(obj -> new SellerStatsDto.CategoryDistributionDto(
                        obj[0] != null ? obj[0].toString() : "Other",
                        ((Number) obj[1]).longValue()
                ))
                .collect(Collectors.toList());

        return SellerStatsDto.builder()
                .totalSalesCount(totalSalesCount != null ? totalSalesCount : 0L)
                .totalRevenue(totalRevenue != null ? 
                        BigDecimal.valueOf(totalRevenue).setScale(2, RoundingMode.HALF_UP).doubleValue() : 0.0)
                .pendingOrderCount(pendingOrderCount != null ? pendingOrderCount : 0L)
                .shippedOrderCount(shippedOrderCount != null ? shippedOrderCount : 0L)
                .deliveredOrderCount(deliveredOrderCount != null ? deliveredOrderCount : 0L)
                .cancelledOrderCount(cancelledOrderCount != null ? cancelledOrderCount : 0L)
                .activeProductsCount(activeProductsCount != null ? activeProductsCount : 0L)
                .dailyRevenue(dailyRevenue)
                .categoryDistribution(categoryDistribution)
                .build();
    }
}
