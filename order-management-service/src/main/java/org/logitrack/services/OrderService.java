package org.logitrack.services;
import org.logitrack.dto.request.OrderRequest;
import org.logitrack.dto.response.ApiResponse;
import org.logitrack.entities.Order;
import org.logitrack.enums.Status;
import org.logitrack.exceptions.CommonApplicationException;
import org.logitrack.exceptions.OrderNotFoundException;
import org.logitrack.enums.OrderProgress;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public interface OrderService {


    ApiResponse createOrder(OrderRequest request,String email);
    ApiResponse<String> updateOrderStatus(Long orderId, Status newStatus, String email);
    ApiResponse<Order> findOrderById(Long orderId) throws OrderNotFoundException;
    ApiResponse<String> deleteOrder(Long orderId, String userEmail);
    ApiResponse<Double> calculateOrderCost(Long orderId);
    ApiResponse saveOrderProgress(Long orderId, OrderProgress orderProgress, String email);

    public ApiResponse<Page<Order>> viewAllOrders(int page, int size) throws OrderNotFoundException;
    ApiResponse<Page<Order>> findOrdersByCustomerId(Long userId, int page, int size) throws OrderNotFoundException;
    ApiResponse<Order> updateOrder(Long orderId, OrderRequest request, String email);
}
