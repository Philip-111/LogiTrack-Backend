package org.logitrack.service;

import org.logitrack.dto.response.ApiResponse;
import org.logitrack.entities.Order;
import org.logitrack.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DeliveryPersonnelService {

    public ApiResponse<String> updateOrderStatus(Long orderId, Status newStatus, String email);
    public ApiResponse<Page<org.logitrack.entities.Order>> getAllAssignedOrders(String email, Pageable pageable);
    public ApiResponse<Order> getAssignedOrderDetails(String email, Long orderId);

}
