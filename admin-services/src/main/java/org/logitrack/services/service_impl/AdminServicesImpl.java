package org.logitrack.services.service_impl;//




import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.logitrack.dto.MessagingObject;
import org.logitrack.dto.Purpose;
import org.logitrack.dto.request.AdminRequestDto;
import org.logitrack.dto.response.AdminDashboardMetrics;
import org.logitrack.dto.response.AdminResponseDto;
import org.logitrack.dto.response.ApiResponse;
import org.logitrack.entities.DeliveryMan;
import org.logitrack.entities.Order;
import org.logitrack.entities.Transaction;
import org.logitrack.entities.User;
import org.logitrack.enums.OrderProgress;
import org.logitrack.enums.Role;
import org.logitrack.enums.Status;
import org.logitrack.exceptions.UserExistException;
import org.logitrack.repository.DeliveryManRepository;
import org.logitrack.repository.OrderRepository;
import org.logitrack.repository.TransactionRepository;
import org.logitrack.repository.UserRepository;
import org.logitrack.services.AdminServices;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service("customAdminService")
@Slf4j
@RequiredArgsConstructor
public class AdminServicesImpl implements AdminServices {

    private final OrderRepository orderRepository;
    private final DeliveryManRepository deliveryManRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${exchange-name}")
    private String exchangeName;

    @Value("${assigned-order-routing-key}")
    private String assignedOrderRoutingKey;

    public ApiResponse<AdminResponseDto> assignOrderToDeliveryPersonnel(String email, AdminRequestDto request) {
        Optional<Order> orderOptional = orderRepository.findById(request.getOrderId());
        Optional<DeliveryMan> deliveryManOptional = deliveryManRepository.findById(request.getDeliveryManId());
        Optional<User> userOptional = userRepository.findByEmail(email);
        User user = userOptional.get();
        if (user.getRole().equals(Role.ADMIN)) {
            if (orderOptional.isPresent()) {
                Order order = orderOptional.get();
                if (deliveryManOptional.isPresent()) {
                    DeliveryMan deliveryMan = deliveryManOptional.get();
                    order.setDeliveryMan(deliveryMan);
                    orderRepository.save(order);
                    log.info("Order with ID:: " + request.getOrderId() + " has been successfully assigned to Delivery Man with ID:: " + request.getDeliveryManId());
                    Order orderResponse = order;
                    AdminResponseDto response = new AdminResponseDto();
                    response.setPickUpAddress(orderResponse.getPickUpAddress());
                    response.setDeliveryAddress(orderResponse.getDeliveryAddress());
                    response.setPackageDetails(orderResponse.getPackageDetails());
                    response.setRecipientName(orderResponse.getRecipientName());
                    response.setRecipientNumber(orderResponse.getRecipientNumber());
                    response.setWeight(orderResponse.getWeight());
                    response.setCreationTime(orderResponse.getCreationTime());
                    response.setPickUpTime(orderResponse.getPickUpTime());
                    response.setInstruction(orderResponse.getInstruction());
                    response.setStatus(orderResponse.getStatus());
                    String message = "Order with ID " + orderResponse.getOrderReference() + " has been successfully assigned to you. Here are the order details:\n"
                            + "Pick-Up Address: " + orderResponse.getPickUpAddress() + "\n"
                            + "Delivery Address: " + orderResponse.getDeliveryAddress() + "\n"
                            + "Package Details: " + orderResponse.getPackageDetails() + "\n"
                            + "Recipient Name: " + orderResponse.getRecipientName() + "\n"
                            + "Recipient Number: " + orderResponse.getRecipientNumber() + "\n"
                            + "Weight: " + orderResponse.getWeight() + "\n"
                            + "Pick-Up Time: " + orderResponse.getPickUpTime() + "\n"
                            + "Instruction: " + orderResponse.getInstruction();

                    MessagingObject msg = MessagingObject.builder()
                            .message(message)
                            .destinationEmail(deliveryMan.getEmail())
                            .purpose(Purpose.ASSIGNED_ORDER)
                            .title("Assign Order")
                            .build();
                    notifyService(msg, assignedOrderRoutingKey);
                    return new ApiResponse<>("00", "Success", response, "Ok", HttpStatus.OK);
                } else {
                    log.error("An error occurred because the delivery man with ID:: " + request.getDeliveryManId() + " Does not exist in the database");
                    return new ApiResponse<>("04", "Delivery man with ID:: " + request.getDeliveryManId() + " cannot be found, please confirm the delivery man ID", HttpStatus.NOT_FOUND);
                }
            } else {
                log.error("An error occurred because the order with ID:: " + request.getOrderId() + "  Does not exist in the database");
                return new ApiResponse<>("04", "Order with ID:: " + request.getOrderId() + " cannot be found, please confirm the order ID before assigning order", HttpStatus.NOT_FOUND);
            }
        } else {
            log.error("A person Which is not an Admin is trying to use this service");
            return new ApiResponse<>("03", "You are not authorized to perform this task", HttpStatus.FORBIDDEN);
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

    @Override
    public ApiResponse<Page<Order>> getOrdersByOrderProgressPaginated(OrderProgress orderProgress, String email, org.springframework.data.domain.Pageable pageable) {
        log.info("Getting orders by order progress: {}", orderProgress);
        // Check the user's role to restrict access if the user is not an admin
        checkUserIsAdmin(email);

        Page<Order> orders = orderRepository.findAllByOrderProgress(orderProgress, pageable);

        if (orders.isEmpty()) {
            log.info("No {} orders found", orderProgress);
            return new ApiResponse<>("404", "No " + orderProgress + " orders found", null, "error", HttpStatus.NOT_FOUND);
        } else {
            log.info("Found orders");
            return new ApiResponse<>("200", "Success", orders, "success", HttpStatus.OK);
        }
    }

    @Override
    public ApiResponse<Page<Order>> viewIncomingOrdersPaginated(String email, Pageable pageable) {
        log.info("Viewing incoming orders");
        // Check the user's role to restrict access if necessary
        checkUserIsAdmin(email);

        Page<Order> orders = orderRepository.findAllByOrderProgress(OrderProgress.NEW, pageable);

        if (orders.isEmpty()) {
            log.info("No new orders found");
            return new ApiResponse<>("404", "No new orders found", HttpStatus.NOT_FOUND);
        } else {
            log.info("Found new orders");

            // Manipulate the creationTime to date-only format
            List<Order> orderList = orders.get()
                    .map(order -> {
                        String formattedDate = order.getCreationTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                        order.setFormattedCreationDate(formattedDate);
                        return order;
                    })
                    .collect(Collectors.toList());

            Page<Order> updatedOrders = new PageImpl<>(orderList, pageable, orders.getTotalElements());

            return new ApiResponse<>("200", "Success", updatedOrders, "success", HttpStatus.OK);
        }

    }
    @Override
    public ApiResponse<String> updateOrderProgress(Long orderId, OrderProgress newProgress, String email) {
        log.info("Updating order progress for order ID: {} to {}", orderId, newProgress);
        // Check the user's role to restrict access if necessary
        checkUserIsAdmin(email);

        Optional<Order> optionalOrder = orderRepository.findById(orderId);

        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            order.setOrderProgress(newProgress);
            orderRepository.save(order);
            log.info("Order progress updated successfully");
            return new ApiResponse<>("200", "Order progress updated successfully", HttpStatus.OK);
        } else {
            log.info("Order not found");
            return new ApiResponse<>("404", "Order not found", HttpStatus.NOT_FOUND);
        }
    }
    @Override
    public ApiResponse<String> updateOrderStatus(Long orderId, Status newStatus, String email) {
        log.info("Updating order status for order ID: {} to {}", orderId, newStatus);
        // Check the user's role to restrict access if necessary
        checkUserIsAdmin(email);
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            order.setStatus(newStatus);
            orderRepository.save(order);
            log.info("Order status updated successfully");
            return new ApiResponse<>("200", "Order status updated successfully", HttpStatus.OK);
        } else {
            log.info("Order not found");
            return new ApiResponse<>("404", "Order not found", HttpStatus.NOT_FOUND);
        }
    }
    @Override
    public ApiResponse<AdminDashboardMetrics> getDashboardMetrics(String email) {
        // Check the user's role to restrict access if necessary
        checkUserIsAdmin(email);
        log.info("Calculating admin dashboard metrics");
        // Populate the AdminDashboardMetrics object after calculations
        BigDecimal totalAmountReceived = calculateTotalAmountReceived();
        long totalNumberOfCustomers = userRepository.findAllByRole(Role.CUSTOMER).size();
        long totalNumberOfOrders = orderRepository.count();
        long totalNumberOfNewUnassignedOrders = orderRepository.countAllByOrderProgress(OrderProgress.NEW);
        BigDecimal totalWeeklyRevenue = calculateTotalWeeklyRevenue();
        BigDecimal totalMonthlyRevenue = calculateTotalMonthlyRevenue();
        BigDecimal totalYearlyRevenue = calculateTotalYearlyRevenue();
        long totalNumberOfCustomersForDay = calculateTotalCustomersForDay();
        long totalNumberOfRidersDeliveredForDay = calculateTotalRidersDeliveredForDay();
        long totalSignUpsForDay = calculateTotalSignUpsForDay();
        long totalFailedOrdersForDay = calculateTotalFailedOrdersForDay();

        AdminDashboardMetrics metrics = AdminDashboardMetrics.builder()
                .totalAmountReceived(totalAmountReceived)
                .totalNumberOfCustomers(totalNumberOfCustomers)
                .totalNumberOfOrders(totalNumberOfOrders)
                .totalNumberOfNewUnassignedOrders(totalNumberOfNewUnassignedOrders)
                .totalWeeklyRevenue(totalWeeklyRevenue)
                .totalMonthlyRevenue(totalMonthlyRevenue)
                .totalYearlyRevenue(totalYearlyRevenue)
                .totalNumberOfCustomersForDay(totalNumberOfCustomersForDay)
                .totalNumberOfRidersDeliveredForDay(totalNumberOfRidersDeliveredForDay)
                .totalSignUpsForDay(totalSignUpsForDay)
                .totalFailedOrdersForDay(totalFailedOrdersForDay)
                .build();
        log.info("Dashboard metrics calculated successfully");
        return new ApiResponse<>("200", "Success", metrics, "success", HttpStatus.OK);
    }

    private void checkUserIsAdmin(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent() && user.get().getRole() == Role.ADMIN) {
            log.info("User with email '{}' is an admin. Access granted.", email);
            return;
        }
        log.info("Access denied. User with email '{}' is not an admin.", email);
        throw new UserExistException("Only admins can access this functionality.");
    }

    private long calculateTotalCustomersForDay() {
        log.info("Calculating total customers for the day");
        LocalDate specificDate = LocalDate.now();
        LocalDate nextDay = specificDate.plusDays(1);
        List<Order> ordersForDay = orderRepository.findAllByCreationTimeBetween(specificDate.atStartOfDay(), nextDay.atTime(LocalTime.MAX));
        Set<String> dailyCustomers = new HashSet<>();
        for (Order order : ordersForDay) {
            if (order.getUser().getRole() == Role.CUSTOMER) {
                dailyCustomers.add(String.valueOf(order.getUser()));
            }
        }
        long totalCustomers = dailyCustomers.size();
        log.info("Total customers for the day: {}", totalCustomers);
        return totalCustomers;
    }


    private long calculateTotalSignUpsForDay() {
        log.info("Calculating total sign-ups for the day");
        LocalDate specificDate = LocalDate.now();
        LocalDate nextDay = specificDate.plusDays(1);
        List<User> newUsersForDay = userRepository.findAllByCreationDateBetween(specificDate.atStartOfDay(), nextDay.atTime(LocalTime.MAX));
        long totalSignUps = newUsersForDay.stream()
                .filter(user -> user.getRole() == Role.CUSTOMER)
                .count();

        log.info("Total sign-ups for the day: {}", totalSignUps);
        return totalSignUps;
    }

    private long calculateTotalFailedOrdersForDay() {
        log.info("Calculating total failed orders for the day");
        LocalDate specificDate = LocalDate.now();
        LocalDate nextDay = specificDate.plusDays(1);
        List<Order> failedOrdersForDay = orderRepository.findAllByStatusAndCreationTimeBetween(Status.FAILED, specificDate.atStartOfDay(), nextDay.atTime(LocalTime.MAX));
        long totalFailedOrders = failedOrdersForDay.size();
        log.info("Total failed orders for the day: {}", totalFailedOrders);
        return totalFailedOrders;
    }

    private long calculateTotalRidersDeliveredForDay() {
        log.info("Calculating total riders delivered for the day");
        LocalDate specificDate = LocalDate.now();
        LocalDate nextDay = specificDate.plusDays(1);
        List<Order> deliveredOrdersForDay = orderRepository.findAllByStatusAndDeliveredAtBetween(Status.DELIVERED, specificDate.atStartOfDay(), nextDay.atTime(LocalTime.MAX));
        Set<String> dailyRiders = new HashSet<>();
        for (Order order : deliveredOrdersForDay) {
            dailyRiders.add(String.valueOf(order.getDeliveryMan()));
        }
        long totalRidersDelivered = dailyRiders.size();

        log.info("Total riders delivered for the day: {}", totalRidersDelivered);
        return totalRidersDelivered;
    }

    private BigDecimal calculateTotalAmountReceived() {
        log.info("Calculating total amount received from transactions");
        List<Transaction> completedTransactions = transactionRepository.findAllByTransactionStatus(Status.SUCCESSFUL);
        BigDecimal totalAmountReceived = completedTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.info("Total amount received calculated successfully: {}", totalAmountReceived);
        return totalAmountReceived;
    }

    private BigDecimal calculateTotalWeeklyRevenue() {
        log.info("Calculating total weekly revenue");
        LocalDate currentDate = LocalDate.now();
        LocalDate weekStartDate = currentDate.minusDays(currentDate.getDayOfWeek().getValue() - 1);
        LocalDate weekEndDate = weekStartDate.plusDays(6);
        List<Transaction> completedTransactions = transactionRepository.findAllByTransactionStatusAndCreatedOnBetween(
                Status.SUCCESSFUL,
                weekStartDate,
                weekEndDate
        );
        BigDecimal totalWeeklyRevenue = completedTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.info("Total weekly revenue calculated successfully: {}", totalWeeklyRevenue);
        return totalWeeklyRevenue;
    }
    private BigDecimal calculateTotalMonthlyRevenue() {
        log.info("Calculating total monthly revenue");
        LocalDate currentDate = LocalDate.now();
        LocalDate monthStartDate = currentDate.withDayOfMonth(1);
        LocalDate monthEndDate = currentDate.withDayOfMonth(currentDate.lengthOfMonth());
        List<Transaction> completedTransactions = transactionRepository.findAllByTransactionStatusAndCreatedOnBetween(
                Status.SUCCESSFUL,
                monthStartDate,
                monthEndDate
        );
        BigDecimal totalMonthlyRevenue = completedTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.info("Total monthly revenue calculated successfully: {}", totalMonthlyRevenue);
        return totalMonthlyRevenue;
    }

    private BigDecimal calculateTotalYearlyRevenue() {
        log.info("Calculating total yearly revenue");
        LocalDate currentDate = LocalDate.now();
        LocalDate yearStartDate = currentDate.withDayOfYear(1);
        LocalDate yearEndDate = currentDate.withDayOfYear(currentDate.lengthOfYear());
        List<Transaction> completedTransactions = transactionRepository.findAllByTransactionStatusAndCreatedOnBetween(
                Status.SUCCESSFUL,
                yearStartDate,
                yearEndDate
        );
        BigDecimal totalYearlyRevenue = completedTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.info("Total yearly revenue calculated successfully: {}", totalYearlyRevenue);
        return totalYearlyRevenue;
    }
}

