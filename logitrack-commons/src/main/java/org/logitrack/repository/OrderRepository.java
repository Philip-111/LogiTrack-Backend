package org.logitrack.repository;

import org.logitrack.entities.Order;
import org.logitrack.entities.User;
import org.logitrack.enums.OrderProgress;
import org.logitrack.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findAllByOrderProgress(OrderProgress orderProgress, Pageable pageable);

    Page<Order> findByUserId(Long userId, Pageable pageable);
//    Optional<Order> findByDeliveryManAndId(Optional<User> user, Long id);

    Page<Order> findByDeliveryMan(Optional<User> user, Pageable pageable);

    Optional<Order> findById(Long orderId);
    long countAllByOrderProgress(OrderProgress orderProgress);
    List<Order> findAllByCreationTimeBetween(LocalDateTime localDateTime, LocalDateTime localDateTime1);
    List<Order> findAllByStatusAndCreationTimeBetween(Status status, LocalDateTime localDateTime, LocalDateTime localDateTime1);
    List<Order> findAllByStatusAndDeliveredAtBetween(Status status, LocalDateTime localDateTime, LocalDateTime localDateTime1);

    Optional<Order> findByDeliveryManAndId(Optional<User> user, Long orderId);
}
