package com.kaan.ecommerce_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDto {
    private double totalSales;
    private long orderCount;
    private long productCount;
    private long customerCount;
    private double monthlyRevenue;

    private List<MonthlyRevenue> last6MonthsRevenue;
    private List<CategoryCount> topCategories;
    private List<RecentOrder> recentOrders;
    private List<LowStockProduct> lowStockProducts;

    @Data
    @AllArgsConstructor
    public static class MonthlyRevenue {
        private String month;
        private double amount;
    }

    @Data
    @AllArgsConstructor
    public static class CategoryCount {
        private String category;
        private long count;
    }

    @Data
    @Builder
    public static class RecentOrder {
        private Long orderId;
        private String customerName;
        private double amount;
        private String status;
        private String date;
    }

    @Data
    @Builder
    public static class LowStockProduct {
        private String asin;
        private String title;
        private int stock;
    }
}

