package org.logitrack.services;

import org.logitrack.dto.request.AdminRequestDto;
import org.logitrack.dto.response.AdminDashboardMetrics;
import org.logitrack.dto.response.AdminResponseDto;
import org.logitrack.dto.response.ApiResponse;
import org.logitrack.entities.Order;
import org.logitrack.enums.OrderProgress;
import org.logitrack.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminServices {
    ApiResponse<AdminResponseDto> assignOrderToDeliveryPersonnel(String email, AdminRequestDto request);
    ApiResponse<Page<Order>> getOrdersByOrderProgressPaginated(OrderProgress orderProgress, String email, org.springframework.data.domain.Pageable pageable);
    ApiResponse<Page<Order>> viewIncomingOrdersPaginated(String email, Pageable pageable);
    ApiResponse<String> updateOrderProgress(Long orderId, OrderProgress newProgress, String email);
    ApiResponse<String> updateOrderStatus(Long orderId, Status newStatus, String email);
    ApiResponse<AdminDashboardMetrics> getDashboardMetrics(String email);
}
