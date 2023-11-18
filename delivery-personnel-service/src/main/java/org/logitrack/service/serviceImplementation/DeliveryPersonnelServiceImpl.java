package org.logitrack.service.serviceImplementation;


import org.logitrack.service.DeliveryPersonnelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.logitrack.dto.response.ApiResponse;
import org.logitrack.entities.Order;
import org.logitrack.entities.User;
import org.logitrack.enums.Role;
import org.logitrack.enums.Status;
import org.logitrack.exceptions.UserExistException;
import org.logitrack.repository.OrderRepository;
import org.logitrack.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryPersonnelServiceImpl implements DeliveryPersonnelService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Override
    public ApiResponse<Page<org.logitrack.entities.Order>> getAllAssignedOrders(String email, Pageable pageable) {
        log.info("Getting orders assigned to delivery personnel {}");
        // check if the user is authorized
        checkUserIsAuthorized(email);
        Optional<User> user = userRepository.findByEmail(email);
        Page<org.logitrack.entities.Order> orders = orderRepository.findByDeliveryMan(user, pageable);

        if (orders.isEmpty()) {
            log.info("No {} orders found", orders);
            return new ApiResponse<>("404", "No " + orders + " orders found", null, "error", HttpStatus.NOT_FOUND);
        } else {
            log.info("orders located");
            return new ApiResponse<>("200", "Success", orders, "success", HttpStatus.OK);
        }

    }


    @Override
    public ApiResponse<org.logitrack.entities.Order> getAssignedOrderDetails(String email, Long orderId) {
        checkUserIsAuthorized(email);
        Optional<User> user = userRepository.findByEmail(email);
        Optional<Order> orders = orderRepository.findByDeliveryManAndId(user, orderId);
        Order assignedOrder = orders.get();
        if (orders.isEmpty()) {
            log.info("No {} orders found", orders);
            return new ApiResponse<>("404", "No " + orders + " orders found", null, "error", HttpStatus.NOT_FOUND);
        } else {
            log.info("orders located");
            return new ApiResponse<>("200", "Success", assignedOrder, "success", HttpStatus.OK);
        }
    }

    @Override
    public ApiResponse<String> updateOrderStatus(Long orderId, Status newStatus, String email) {
        log.info("Updating order status for order ID: {} to {}", orderId, newStatus);
        // Check the users role
        checkUserIsAuthorized(email);
        Optional<User> user = userRepository.findByEmail(email);
        Optional<org.logitrack.entities.Order> fetchOrder = orderRepository.findByDeliveryManAndId(user, orderId);

        if (fetchOrder.isPresent()) {
            org.logitrack.entities.Order order = fetchOrder.get();
            order.setStatus(newStatus);
            orderRepository.save(order);
            log.info("Order status updated successfully by delivey personnel");
            return new ApiResponse<>("200", "Order status updated successfully", HttpStatus.OK);
        } else {
            log.info("Order not found");
            return new ApiResponse<>("404", "Order not found", HttpStatus.NOT_FOUND);
        }
    }


    private void checkUserIsAuthorized(String email) {

        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent() && user.get().getRole() == Role.ADMIN || user.isPresent() && user.get().getRole() == Role.DELIVERY_MAN) {
            log.info("User with email '{}' has access to make this change..access granted!!.", email);
            return;
        }
        log.info("Access denied. User with email '{}' is not an admin.", email);
        throw new UserExistException("Only authorized personnels can access this functionality.");
    }
}
