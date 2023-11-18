package org.logitrack.services.serviceImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.logitrack.dto.MessagingObject;
import org.logitrack.dto.Purpose;
import org.logitrack.dto.request.OrderRequest;
import org.logitrack.dto.response.ApiResponse;
import org.logitrack.entities.Order;
import org.logitrack.entities.User;
import org.logitrack.enums.OrderProgress;
import org.logitrack.enums.Role;
import org.logitrack.enums.Status;
import org.logitrack.exceptions.OrderNotFoundException;
import org.logitrack.exceptions.UserExistException;
import org.logitrack.repository.OrderRepository;
import org.logitrack.repository.UserRepository;
import org.logitrack.services.OrderService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${PRICING_COST_PER_KM}")
    private double costPerKilometer;

    @Value("${PRICING_FLAT_RATE_WEIGHT_THRESHOLD}")
    private double flatRateWeightThreshold;

    @Value("${PRICING_FLAT_RATE_COST}")
    private double flatRateCost;

    @Value("${PRICING_ADDITIONAL_COST_PER_KILOGRAM}")
    private double additionalCostPerKilogram;
    private static final double EARTH_RADIUS_KM = 6371.0; // Earth's radius in kilometers

    @Value("${exchange-name}")
    private String exchangeName;

    @Value("${order-status-routing-key}")
    private String orderStatusRoutingKey;

    @Value("${order-created-routing-key}")
    private String orderCreatedRoutingKey;

    @Override
    public ApiResponse<Order> createOrder(OrderRequest request, String email) {
        log.info("Checking if user exists");
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return new ApiResponse<>("00", "User not found", HttpStatus.NOT_FOUND);
        }
        User user = userOptional.get();
        log.info("The role of the user is " + user.getRole().name());
        if (user.getRole().equals(Role.CUSTOMER)) {
            Order order = new Order();
            order.setOrderReference(order.getOrderReference());
            order.setPickUpAddress(request.getPickUpAddress());
            order.setDeliveryAddress(request.getDeliveryAddress());
            order.setPackageDetails(request.getPackageInfo());
            order.setDeliveryAddressTextFormat(request.getDeliveryAddressTextFormat());
            order.setPickUpAddressTextFormat(request.getPickUpAddressTextFormat());
            order.setRecipientName(request.getRecipientName());
            order.setRecipientNumber(request.getRecipientNumber());
            order.setWeight(request.getWeight());
            order.setStatus(Status.PENDING);
            LocalDate localDate = LocalDate.parse(request.getPickUpTime(), formatter);
            LocalTime localTime = LocalTime.of(0, 0); // midnight (00:00:00)
            // Combine LocalDate and LocalTime to create LocalDateTime
            LocalDateTime pickUpTime = LocalDateTime.of(localDate, localTime);
            order.setPickUpTime(pickUpTime);
            order.setInstruction(request.getInstruction());
            order.setOrderProgress(OrderProgress.NEW);
            order.setCreationTime(LocalDateTime.now());
            order.setUser(user);
            Order createdOrder = orderRepository.save(order);
            String message = "A new order with ID  " + createdOrder.getOrderReference() + " has been created.";
            MessagingObject msg = MessagingObject.builder()
                    .message(message)
                    .destinationEmail(user.getEmail())
                    .purpose(Purpose.ORDER_CREATED)
                    .title("New Order Created")
                    .build();
            notifyService(msg, orderCreatedRoutingKey);
            return new ApiResponse<>("00", "Order created successfully", createdOrder, "OK");
        } else {
            return new ApiResponse<>("00", "User is not a customer", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ApiResponse<Order> updateOrder(Long orderId, OrderRequest request, String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return new ApiResponse<>("10", "User not found", HttpStatus.NOT_FOUND);
        }
        User user = userOptional.get();
        ApiResponse<Order> orderOptional = findOrderById(orderId);
        Order existingOrder = orderOptional.getData();

        existingOrder.setOrderReference(existingOrder.getOrderReference());
        existingOrder.setPickUpAddress(request.getPickUpAddress());
        existingOrder.setDeliveryAddress(request.getDeliveryAddress());
        existingOrder.setPackageDetails(request.getPackageInfo());
        existingOrder.setDeliveryAddressTextFormat(request.getDeliveryAddressTextFormat());
        existingOrder.setPickUpAddressTextFormat(request.getPickUpAddressTextFormat());
        existingOrder.setRecipientName(request.getRecipientName());
        existingOrder.setRecipientNumber(request.getRecipientNumber());
        existingOrder.setWeight(request.getWeight());
        existingOrder.setStatus(Status.PENDING);
        LocalDate localDate = LocalDate.parse(request.getPickUpTime(), formatter);
        LocalTime localTime = LocalTime.of(0, 0);
        LocalDateTime pickUpTime = LocalDateTime.of(localDate, localTime);
        existingOrder.setPickUpTime(pickUpTime);
        existingOrder.setInstruction(request.getInstruction());
        existingOrder.setOrderProgress(OrderProgress.NEW);
        existingOrder.setCreationTime(LocalDateTime.now());
        existingOrder.setUser(user);

        Order updatedOrder = orderRepository.save(existingOrder);

        return new ApiResponse<>("00", "Order created successfully", updatedOrder, "OK");
    }

    @Override
    public ApiResponse<Order> findOrderById(Long orderId) throws OrderNotFoundException {
        Optional<Order> orderOptional = orderRepository.findById(orderId);

        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();
            return new ApiResponse<>("00", "Success", order, "OK");
        } else {
            throw new OrderNotFoundException("Order does not exist");
        }
    }

    @Override
    public ApiResponse<String> deleteOrder(Long orderId, String userEmail) {
        log.info("Checking if order exists");
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isEmpty()) {
            log.error("Order not found for ID: {}", orderId);
            return new ApiResponse<>("10", "Order not found", HttpStatus.NOT_FOUND);
        }
        Order order = optionalOrder.get();
        log.info("Checking if the user has permission to delete the order");

        if (!userEmail.equals(order.getUserEmail())) {
            log.error("User is not the creator of this order");
            return new ApiResponse<>("10", "User is not the creator of this order", HttpStatus.FORBIDDEN);
        }

        Status orderStatus = order.getStatus();

        if (orderStatus == Status.PICKED_UP || orderStatus == Status.DELIVERED || orderStatus == Status.IN_PROGRESS) {
            log.error("Orders with status '{}' cannot be deleted", orderStatus);
            return new ApiResponse<>("10", "Orders with status '" + orderStatus + "' cannot be deleted", HttpStatus.FORBIDDEN);
        }

        log.info("Deleting the order");
        orderRepository.deleteById(orderId);
        log.info("Order deleted successfully");

        return new ApiResponse<>("00", "Order deleted successfully", HttpStatus.OK);
    }

    @Override
    public ApiResponse<String> updateOrderStatus(Long orderId, Status newStatus, String email) {
        log.info("Updating order status for order ID: {} to {}", orderId, newStatus);
        checkUserIsAdminOrDeliveryPersonnel(email);
        Optional<Order> optionalOrder = orderRepository.findById(orderId);

        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            order.setStatus(newStatus);
            orderRepository.save(order);
            log.info("Order status updated successfully");
            String message = "Your order with ID " + order.getOrderReference() + " has been " + newStatus;
            MessagingObject msg = MessagingObject.builder()
                    .message(message)
                    .destinationEmail(order.getUserEmail())
                    .purpose(Purpose.ORDER_STATUS)
                    .title("Order " + newStatus).build();
            notifyService(msg, orderStatusRoutingKey);
            return new ApiResponse<>("00", "Order status updated successfully", HttpStatus.OK);
        } else {
            log.info("Order not found");
            return new ApiResponse<>("404", "Order not found", HttpStatus.NOT_FOUND);
        }
    }

    private void notifyService(MessagingObject message, String routingKey) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            rabbitTemplate.convertAndSend(exchangeName, routingKey, jsonMessage);
            log.info("Order notification pushed successfully");
        } catch (JsonProcessingException e) {
            log.error("Error serializing the message: {}", e.getMessage());
        }
    }

    private void checkUserIsAdminOrDeliveryPersonnel(String email) {
        Optional<User> user = userRepository.findByEmail(email);

        if (user.isPresent()) {
            Role userRole = user.get().getRole();

            if (userRole == Role.ADMIN || userRole == Role.DELIVERY_MAN) {
                log.info("User with email '{}' has the role '{}'. Access granted.", email, userRole);
                return;
            }
        }
        log.info("Access denied. User with email '{}' is not an admin or delivery personnel.", email);
        throw new UserExistException("Only admins and delivery personnel can access this functionality.");
    }

    @Override
    public ApiResponse<Double> calculateOrderCost(Long orderId) {
        Order createdOrder = orderRepository.findById(orderId).orElseThrow(OrderNotFoundException::new);

        double pickupLatitude = extractLatitude(createdOrder.getPickUpAddress());
        double pickupLongitude = extractLongitude(createdOrder.getPickUpAddress());
        double deliveryLatitude = extractLatitude(createdOrder.getDeliveryAddress());
        double deliveryLongitude = extractLongitude(createdOrder.getDeliveryAddress());

        log.info("Calculating order cost for pickup latitude: {}, longitude: {}", pickupLatitude, pickupLongitude);
        log.info("Calculating order cost for delivery latitude: {}, longitude: {}", deliveryLatitude, deliveryLongitude);

        double distance = calculateDistance(
                pickupLatitude,
                pickupLongitude,
                deliveryLatitude,
                deliveryLongitude
        );
        log.info("Calculated distance: {} km", distance);

        Double weightCost = calculateWeightCost((long) createdOrder.getWeight());
        log.info("Calculated weight cost: {}", weightCost);

        Double totalCost = distance * costPerKilometer + weightCost;
        log.info("Calculated total cost: {}", totalCost);

        createdOrder.setAmount(totalCost);
        orderRepository.save(createdOrder);

        return new ApiResponse<>("00", "Success", totalCost, "OK");
    }

    private long calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        double longitude = lon2Rad - lon1Rad;
        double latitude = lat2Rad - lat1Rad;
        double a = Math.sin(latitude / 2) * Math.sin(latitude / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(longitude / 2) * Math.sin(longitude / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distanceInKilometers = EARTH_RADIUS_KM * c;
        return Math.round(distanceInKilometers);
    }

    private Double calculateWeightCost(Long weight) {
        if (weight <= flatRateWeightThreshold) {
            return flatRateCost;
        } else {
            return flatRateCost + (weight - flatRateWeightThreshold) * additionalCostPerKilogram;
        }
    }

    private double extractLatitude(String address) {
        String[] location = address.split(",");
        return Double.parseDouble(location[0]);
    }

    private double extractLongitude(String address) {
        String[] location = address.split(",");
        return Double.parseDouble(location[1]);
    }


    @Override
    public ApiResponse saveOrderProgress(Long orderId, OrderProgress orderProgress, String email) {
        log.info("checking if user exist");
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            return new ApiResponse<>("00", "User not found", HttpStatus.NOT_FOUND);
        }
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isEmpty()) {
            return new ApiResponse<>("00", "order not found", HttpStatus.NOT_FOUND);
        }
        if (orderProgress.equals("NEW")) {
            optionalOrder.get().setOrderProgress(OrderProgress.NEW);
            optionalOrder.get().setCreationTime(null);
            orderRepository.save(optionalOrder.get());
            return new ApiResponse("01", "Request Processed Successfully", HttpStatus.OK);
        }
        if (orderProgress.equals("PENDING")) {
            optionalOrder.get().setOrderProgress(OrderProgress.PENDING);
            optionalOrder.get().setCreationTime(null);
            orderRepository.save(optionalOrder.get());
            return new ApiResponse("01", "Request Processed Successfully", HttpStatus.OK);
        }
        if (orderProgress.equals("COMPLETED")) {
            optionalOrder.get().setOrderProgress(OrderProgress.COMPLETED);
            optionalOrder.get().setCreationTime(LocalDateTime.now());
            orderRepository.save(optionalOrder.get());
        }
        return new ApiResponse<>("01", "Request Processed Successfully", HttpStatus.OK);
    }

    @Override
    public ApiResponse<Page<Order>> viewAllOrders(int page, int size) throws OrderNotFoundException {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = orderRepository.findAll(pageable);

        if (orders.hasContent()) {
            return new ApiResponse<>("00", "Success", orders, "OK");
        } else {
            throw new OrderNotFoundException("No orders found");
        }
    }

    @Override
    public ApiResponse<Page<Order>> findOrdersByCustomerId(Long userId, int page, int size) throws OrderNotFoundException {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = orderRepository.findByUserId(userId, pageable);

        if (orders.hasContent()) {
            return new ApiResponse<>("00", "Success", orders, "OK");
        } else {
            throw new OrderNotFoundException("No orders found for customer ID: " + userId);
        }
    }
}
