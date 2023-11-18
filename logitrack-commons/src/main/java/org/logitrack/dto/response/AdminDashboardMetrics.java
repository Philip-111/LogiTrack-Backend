package org.logitrack.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@AllArgsConstructor
@Builder
public class AdminDashboardMetrics {

    private BigDecimal totalAmountReceived;
    private long totalNumberOfCustomers;
    private long totalNumberOfOrders;
    private long totalNumberOfNewUnassignedOrders;
    private BigDecimal totalWeeklyRevenue;
    private BigDecimal totalMonthlyRevenue;
    private BigDecimal totalYearlyRevenue;
    private long totalNumberOfCustomersForDay;
    private long totalNumberOfRidersDeliveredForDay;
    private long totalSignUpsForDay;
    private long totalFailedOrdersForDay;

}
