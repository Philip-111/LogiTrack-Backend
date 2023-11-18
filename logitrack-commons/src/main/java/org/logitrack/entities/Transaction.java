package org.logitrack.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.logitrack.enums.Status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @Column
    private String AddressDelivered;

    @Column
    private String recipientName;

    @Column
    private Status transactionStatus;

    @Column
    private String transactionReference;

    @Column
    private BigDecimal amount;

    @Column
    private String gatewayResponse;

    @Column
    private LocalTime completedAt;

    @Column
    private LocalTime createdAt;

    @Column
    private LocalDate createdOn;
}
