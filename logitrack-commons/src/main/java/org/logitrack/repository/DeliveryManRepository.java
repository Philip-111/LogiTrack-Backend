package org.logitrack.repository;

import org.logitrack.entities.DeliveryMan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DeliveryManRepository extends JpaRepository<DeliveryMan, Long> {
    Optional<DeliveryMan> findByEmail(String email);

}
