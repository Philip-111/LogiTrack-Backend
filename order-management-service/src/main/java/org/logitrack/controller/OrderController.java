package org.logitrack.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.logitrack.dto.request.OrderRequest;
import org.logitrack.dto.response.ApiResponse;
import org.logitrack.entities.Order;
import org.logitrack.enums.OrderProgress;
import org.logitrack.enums.Status;
import org.logitrack.exceptions.CommonApplicationException;
import org.logitrack.exceptions.OrderNotFoundException;
import org.logitrack.securities.JWTService;
import org.logitrack.services.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/order")
public class OrderController {
    private final OrderService orderService;
    private final JWTService jwtService;

    @PostMapping("/create-order")
    public ResponseEntity<ApiResponse> createOrder(@Valid @RequestBody OrderRequest request, @RequestHeader("Authorization") String authorizationHeader) throws CommonApplicationException {
        var userDetails = jwtService.validateTokenAndReturnDetail(authorizationHeader.substring(7));
        log.info("request for customer {} to create order", userDetails.get("name"));
        ApiResponse apiResponse = orderService.createOrder(request, (String) userDetails.get("email"));
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @GetMapping("/calculate-order-cost")
    public ResponseEntity<ApiResponse<Double>> calculateOrderCost(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam("orderId") Long orderId
    ) throws CommonApplicationException {
        var userDetails = jwtService.validateTokenAndReturnDetail(authorizationHeader.substring(7));
        log.info("Request for customer {} to calculate order", userDetails.get("name"));
        ApiResponse<Double> apiResponse = orderService.calculateOrderCost(orderId);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<Order>> viewOrderById(
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String authorizationHeader
    ) throws OrderNotFoundException, CommonApplicationException {
        var userDetails = jwtService.validateTokenAndReturnDetail(authorizationHeader.substring(7));
        log.info("User {} is viewing order with ID {}", userDetails.get("name"), orderId);
        ApiResponse<Order> order = orderService.findOrderById(orderId);
        return new ResponseEntity<>(order, HttpStatus.FOUND);
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<String>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam("newStatus") Status newStatus,
            @RequestHeader("Authorization") String authorizationHeader
    ) throws CommonApplicationException {
        var userDetails = jwtService.validateTokenAndReturnDetail(authorizationHeader.substring(7));
        log.info("Request for customer {} to update order status", userDetails.get("name"));
        ApiResponse<String> apiResponse = orderService.updateOrderStatus(orderId, newStatus, (String) userDetails.get("email"));
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PatchMapping("/save-order-progress/{orderId}/")
    public ResponseEntity<ApiResponse> saveOrderProgress(@PathVariable Long orderId, @RequestParam("orderProgress") OrderProgress orderProgress, @RequestHeader("Authorization") String authorizationHeader)
            throws CommonApplicationException {
        var userDetails = jwtService.validateTokenAndReturnDetail(authorizationHeader.substring(7));
        log.info("request for customer {} to create order", userDetails.get("name"));
        ApiResponse apiResponse = orderService.saveOrderProgress(orderId, orderProgress, (String) userDetails.get("email"));
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/all-orders")
    public ResponseEntity<ApiResponse<Page<Order>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("Authorization") String authorizationHeader
    ) throws OrderNotFoundException, CommonApplicationException {
        var userDetails = jwtService.validateTokenAndReturnDetail(authorizationHeader.substring(7));
        log.info("User {} is retrieving all orders", userDetails.get("name"));
        ApiResponse<Page<Order>> orders = orderService.viewAllOrders(page, size);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @GetMapping("/orders-by-customer/{customerId}")
    public ResponseEntity<ApiResponse<Page<Order>>> getOrdersByCustomer(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader(name = "Authorization", required = false) HttpHeaders headers
    ) throws OrderNotFoundException, CommonApplicationException {
        var userDetails = jwtService.validateTokenAndReturnDetail(headers.getFirst("Authorization").substring(7));
        log.info("User {} is retrieving orders for customer ID {}", userDetails.get("name"), customerId);
        ApiResponse<Page<Order>> orders = orderService.findOrdersByCustomerId(customerId, page, size);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<ApiResponse<String>> deleteOrder(
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String authorizationHeader
    ) throws CommonApplicationException {
        log.info("Received request with Authorization Header: {}", authorizationHeader);
        var userDetails = jwtService.validateTokenAndReturnDetail(authorizationHeader.substring(7));
        log.info("Request for customer {} to delete an order", userDetails.get("name"));
        String userEmail = userDetails.get("email");
        ApiResponse<String> apiResponse = orderService.deleteOrder(orderId, userEmail);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PutMapping("/update/{orderId}")
    public ResponseEntity<ApiResponse<Order>> updateOrder(
            @PathVariable Long orderId,
            @RequestBody OrderRequest request, @RequestHeader("Authorization") String authorizationHeader) throws CommonApplicationException {
        var userDetails = jwtService.validateTokenAndReturnDetail(authorizationHeader.substring(7));
        log.info("Request for customer {} to delete an order", userDetails.get("name"));
        String userEmail = userDetails.get("email");
        ApiResponse<Order> updatedOrder = orderService.updateOrder(orderId, request, userEmail);
        return new ResponseEntity<>(updatedOrder, HttpStatus.CREATED);
    }

}

