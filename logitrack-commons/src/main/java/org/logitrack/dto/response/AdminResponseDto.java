package org.logitrack.dto.response;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.logitrack.enums.Status;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class AdminResponseDto {

    private String pickUpAddress;

    private String deliveryAddress;

    private String packageDetails;

    private String recipientName;

    private String recipientNumber;

    private Double weight;

    private LocalDateTime creationTime;

    private LocalDateTime pickUpTime;

    private String instruction;

    private Status status;

}
