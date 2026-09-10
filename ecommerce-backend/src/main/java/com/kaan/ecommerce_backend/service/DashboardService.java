package com.kaan.ecommerce_backend.service;

import com.kaan.ecommerce_backend.dto.DashboardStatsDto;

import com.kaan.ecommerce_backend.entity.Order;
import com.kaan.ecommerce_backend.entity.Product;
import com.kaan.ecommerce_backend.repository.OrderRepository;
import com.kaan.ecommerce_backend.repository.ProductRepository;
import com.kaan.ecommerce_backend.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public DashboardService(OrderRepository orderRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public DashboardStatsDto getDashboardStats() {
        Double totalSales = orderRepository.sumTotalAmount();
        Double monthlyRevenue = orderRepository.sumTotalAmountSince(LocalDateTime.now().minusDays(30));

        List<DashboardStatsDto.MonthlyRevenue> last6MonthsRevenue = calculateLast6MonthsRevenue();

        List<DashboardStatsDto.CategoryCount> topCategories = productRepository.getCategoryCounts(PageRequest.of(0, 5));

        List<DashboardStatsDto.RecentOrder> recentOrders = orderRepository.findTop5ByOrderByOrderDateDesc().stream()
                .map(order -> DashboardStatsDto.RecentOrder.builder()
                        .orderId(order.getId())
                        .customerName(order.getUser().getFirstName() + " " + order.getUser().getLastName())
                        .amount(round(order.getTotalAmount()))
                        .status(order.getStatus())
                        .date(order.getOrderDate().toString())
                        .build())
                .collect(Collectors.toList());

        List<DashboardStatsDto.LowStockProduct> lowStockProducts = productRepository.findTop5ByStockLessThanOrderByStockAsc(5).stream()
                .map(product -> DashboardStatsDto.LowStockProduct.builder()
                        .asin(product.getAsin())
                        .title(product.getTitle())
                        .stock(product.getStock())
                        .build())
                .collect(Collectors.toList());

        return DashboardStatsDto.builder()
                .totalSales(totalSales != null ? round(totalSales) : 0.0)
                .monthlyRevenue(monthlyRevenue != null ? round(monthlyRevenue) : 0.0)
                .orderCount(orderRepository.count())
                .productCount(productRepository.count())
                .customerCount(userRepository.countByRole("USER"))
                .last6MonthsRevenue(last6MonthsRevenue)
                .topCategories(topCategories)
                .recentOrders(recentOrders)
                .lowStockProducts(lowStockProducts)
                .build();
    }

    private List<DashboardStatsDto.MonthlyRevenue> calculateLast6MonthsRevenue() {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6).withDayOfMonth(1).withHour(0).withMinute(0);
        List<Order> recentOrders = orderRepository.findAllSince(sixMonthsAgo);

        Map<String, Double> revenueByMonth = new LinkedHashMap<>();
        
        for (int i = 5; i >= 0; i--) {
            String monthName = LocalDateTime.now().minusMonths(i).getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            revenueByMonth.put(monthName, 0.0);
        }

        for (Order order : recentOrders) {
            String monthName = order.getOrderDate().getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            if (revenueByMonth.containsKey(monthName)) {
                revenueByMonth.put(monthName, revenueByMonth.get(monthName) + order.getTotalAmount());
            }
        }

        return revenueByMonth.entrySet().stream()
                .map(entry -> new DashboardStatsDto.MonthlyRevenue(entry.getKey(), round(entry.getValue())))
                .collect(Collectors.toList());
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}


