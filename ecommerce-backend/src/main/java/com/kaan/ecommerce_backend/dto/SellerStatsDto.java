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
public class SellerStatsDto {
    private long totalSalesCount;
    private double totalRevenue;
    private long pendingOrderCount;
    private long shippedOrderCount;
    private long deliveredOrderCount;
    private long cancelledOrderCount;
    private long activeProductsCount;

    private List<DailyRevenueDto> dailyRevenue;
    private List<CategoryDistributionDto> categoryDistribution;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DailyRevenueDto {
        private String date;
        private double revenue;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategoryDistributionDto {
        private String category;
        private long count;
    }
}
