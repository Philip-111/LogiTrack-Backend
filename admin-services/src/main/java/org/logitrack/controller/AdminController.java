package org.logitrack.controller;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.logitrack.dto.request.AdminRequestDto;
import org.logitrack.dto.request.DeliveryManCreationRequest;
import org.logitrack.dto.response.AdminDashboardMetrics;
import org.logitrack.dto.response.AdminResponseDto;
import org.logitrack.dto.response.ApiResponse;
import org.logitrack.entities.User;
import org.logitrack.enums.Role;
import org.logitrack.exceptions.CommonApplicationException;
import org.logitrack.securities.JWTService;
import org.logitrack.services.AdminServices;
import org.logitrack.services.CustomerService;
import org.logitrack.utils.Utils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.logitrack.entities.Order;
import org.logitrack.enums.OrderProgress;
import org.logitrack.enums.Status;
import org.logitrack.exceptions.OrderNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
@RestController
@RequestMapping({"/api/v1/admin"})
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final AdminServices adminServices;
    private final CustomerService userService;
    private final JWTService jwtService;

    @PostMapping("/create-delivery-man")
    public ResponseEntity<?> createDeliveryMan(@RequestBody DeliveryManCreationRequest request,
                                               @RequestHeader("Authorization") String authorizationHeader)
            throws CommonApplicationException {
        var userDetails=jwtService.validateTokenAndReturnDetail(authorizationHeader.substring(7));
        ApiResponse response = userService.registerDeliveryMan(request, userDetails.get("email"));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @GetMapping("/all-customers")
    public ResponseEntity<Page<User>> getAllCustomers(Pageable pageable, @RequestHeader("Authorization") String authorizationHeader)
            throws CommonApplicationException {
        var userDetails=jwtService.validateTokenAndReturnDetail(authorizationHeader.substring(7));
        Page<User> getAllCustomer = userService.fetchByRole(Role.CUSTOMER, pageable, userDetails.get("email"));
        return ResponseEntity.ok(getAllCustomer);
    }

    @GetMapping("/all-riders")
    public ResponseEntity<Page<User>> getAllRiders(Pageable pageable, @RequestHeader("Authorization") String authorizationHeader)
            throws CommonApplicationException {
        var userDetails=jwtService.validateTokenAndReturnDetail(authorizationHeader.substring(7));
        Page<User> getAllCustomer = userService.fetchByRole(Role.DELIVERY_MAN, pageable, userDetails.get("email"));
        return ResponseEntity.ok(getAllCustomer);
    }

    @PostMapping("/assign-order-to-deliveryman")
    public ResponseEntity<ApiResponse<AdminResponseDto>> assignOrderToDeliveryPersonnel
            ( @RequestBody AdminRequestDto request,
              @RequestHeader("Authorization") String authorizationHeader) throws CommonApplicationException {
        var userDetails= jwtService.validateTokenAndReturnDetail(authorizationHeader.substring(7));
        log.info("Admin with ID:: " + userDetails.get("email")+" wants to assign order to a delivery man");
        ApiResponse<AdminResponseDto> response = adminServices.assignOrderToDeliveryPersonnel(userDetails.get("email"), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    @GetMapping("/incomingOrders")
    public ResponseEntity<ApiResponse<Page<Order>>> viewIncomingOrders(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) throws OrderNotFoundException, CommonApplicationException {
        var userDetails = jwtService.validateTokenAndReturnDetail(authorizationHeader.substring(7));
        Pageable pageable = PageRequest.of(page, size);
        ApiResponse<Page<Order>> response = adminServices.viewIncomingOrdersPaginated(userDetails.get("email"), pageable);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/ordersByOrderProgress")
    public ResponseEntity<ApiResponse<Page<Order>>> getOrdersByOrderProgress(
            @RequestParam OrderProgress orderProgress,
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) throws OrderNotFoundException, CommonApplicationException {
        var userDetails = jwtService.validateTokenAndReturnDetail(authorizationHeader.substring(7));
        Pageable pageable = PageRequest.of(page, size);
        ApiResponse<Page<Order>> response = adminServices.getOrdersByOrderProgressPaginated(orderProgress, (String) userDetails.get("email"), pageable);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/updateOrderProgress/{orderId}")
    public ResponseEntity<ApiResponse<String>> updateOrderProgress(
            @PathVariable Long orderId,
            @RequestParam OrderProgress newProgress,
            @RequestHeader("Authorization") String authorizationHeader) throws OrderNotFoundException, CommonApplicationException {
        var userDetails = jwtService.validateTokenAndReturnDetail(authorizationHeader.substring(7));
        ApiResponse<String> response = adminServices.updateOrderProgress(orderId, newProgress, (String) userDetails.get("email"));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<String>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam("newStatus") Status newStatus,
            @RequestHeader("Authorization") String authorizationHeader
    ) throws CommonApplicationException {
        var userDetails = jwtService.validateTokenAndReturnDetail(authorizationHeader.substring(7));
        ApiResponse<String> apiResponse = adminServices.updateOrderStatus(orderId, newStatus, (String) userDetails.get("email"));
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/dashboard-metrics")
    public ResponseEntity<ApiResponse<AdminDashboardMetrics>> getDashboardMetrics(
            @RequestHeader("Authorization") String authorizationHeader
    ) throws CommonApplicationException {
        var userDetails = Utils.validateTokenAndReturnDetail(authorizationHeader.substring(7));
        ApiResponse<AdminDashboardMetrics> response = adminServices.getDashboardMetrics((String) userDetails.get("email"));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request, String authorizationHeader) throws CommonApplicationException {
        ApiResponse<String> response = userService.logout(request, authorizationHeader);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
}
