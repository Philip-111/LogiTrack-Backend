package org.logitrack.entities;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.logitrack.enums.OrderProgress;
import org.logitrack.enums.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Entity
@Table(name = "orders")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String orderReference;

    @Column(nullable = false)
    private String pickUpAddress;

    @Column(nullable = false)
    private String deliveryAddress;

    @Column(nullable = false)
    private String pickUpAddressTextFormat;

    @Column(nullable = false)
    private String deliveryAddressTextFormat;

    @Column(nullable = false)
    private String packageDetails;

    @Column(nullable = false)
    private String recipientName;

    @Column(nullable = false)
    private String recipientNumber;

    @Column(nullable = false)
    private double weight;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime creationTime;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime pickUpTime;

    @Column(nullable = false)
    private String instruction;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderProgress orderProgress;

    @Column(nullable = false)
    private String userEmail;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    @JsonIgnore
    private User user;

    @ManyToOne
    @JoinColumn(name = "delivery_man_id")
    @JsonIgnore
    private DeliveryMan deliveryMan;

    @Column
    private String orderBatchId;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime deliveredAt;

    @PrePersist
    private void prePersist() {
        if (user != null) {
            userEmail = user.getEmail();
        }
        orderReference = generateOrderReference();
    }
    private String generateOrderReference() {
        String formattedCreationTime = creationTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "ORD" + "-" + formattedCreationTime + "-" + generateNumericOTP();
    }
    private String generateNumericOTP() {
        Random random = new Random();
        int otpLength = 6;
        StringBuilder otpBuilder = new StringBuilder();

        for (int i = 0; i < otpLength; i++) {
            int digit = random.nextInt(10);
            otpBuilder.append(digit);
        }

        return otpBuilder.toString();
    }

    @Column
    private Double amount;

    private String formattedCreationDate;

    public void setFormattedCreationDate(String formattedCreationDate) {
        this.formattedCreationDate = formattedCreationDate;
    }
}
