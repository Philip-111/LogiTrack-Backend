package org.logitrack.repository;

import org.logitrack.entities.Transaction;
import org.logitrack.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Component
public interface TransactionRepository extends JpaRepository<Transaction,Long> {

    Optional<Transaction> findTransactionByTransactionReference(String reference);
    List<Transaction> findAllByTransactionStatus(Status status);
    List<Transaction> findAllByTransactionStatusAndCreatedOnBetween(Status status, LocalDate weekStartDate, LocalDate weekEndDate);
}
