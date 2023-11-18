package org.logitrack.controller;


import org.logitrack.dto.UpdateStatusDto;
import org.logitrack.service.DeliveryPersonnelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.logitrack.dto.response.ApiResponse;
import org.logitrack.entities.Order;
import org.logitrack.enums.Status;
import org.logitrack.exceptions.CommonApplicationException;
import org.logitrack.securities.JWTService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/delivery")
public class DeliveryPersonnelController {

    private final DeliveryPersonnelService deliveryPersonnelService;
    private final JWTService jwtService;


    // Endpoint for getting assigned orders
    @GetMapping("/assigned-orders")
    public ResponseEntity<ApiResponse<Page<org.logitrack.entities.Order>>> getAssignedOrders(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    )throws CommonApplicationException {
        var userDetails=jwtService.validateTokenAndReturnDetail(authorizationHeader.substring(7));
        log.info("rider {} is viewing orders assigned to him", userDetails.get("name"));
        Pageable pageable = PageRequest.of(page, size);
        ApiResponse<Page<org.logitrack.entities.Order>> assignedOrders = deliveryPersonnelService.getAllAssignedOrders((String) userDetails.get("email"), pageable);
        return ResponseEntity.ok(assignedOrders);
    }

    // Endpoint for updating order delivery status
    @PutMapping("/update-status")
    public ResponseEntity<ApiResponse<String>> updateOrderStatus(@RequestBody UpdateStatusDto statusDto,
                                                                 @RequestHeader("Authorization") String authorizationHeader

    ) throws CommonApplicationException {
        var userDetails=jwtService.validateTokenAndReturnDetail(authorizationHeader.substring(7));
        log.info("rider {} is viewing orders assigned to him", userDetails.get("name"));
        ApiResponse<String> updated = deliveryPersonnelService.updateOrderStatus(statusDto.getOrderId(), statusDto.getNewStatus(), (String) userDetails.get("email"));
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    // Endpoint for getting assigned order details
    @GetMapping("/assigned-order-details/{orderId}")
    public ResponseEntity<ApiResponse<Order>> getAssignedOrdersDetails(@PathVariable Long orderId,
                                                                       @RequestHeader("Authorization") String authorizationHeader)
            throws CommonApplicationException {
        var userDetails=jwtService.validateTokenAndReturnDetail(authorizationHeader.substring(7));
        log.info("rider {} is viewing the details of order assigned to him", userDetails.get("name"));
        //ApiResponse<Order> assignedOrders = deliveryPersonnelService.getAssignedOrderDetails(orderId, String.valueOf(userDetails.get("email")));
        ApiResponse<org.logitrack.entities.Order> orderDetails = deliveryPersonnelService.getAssignedOrderDetails((String) userDetails.get("email"), orderId);
        //return ResponseEntity.ok(assignedOrders);
        if (orderDetails != null) {
            return ResponseEntity.ok(orderDetails);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
