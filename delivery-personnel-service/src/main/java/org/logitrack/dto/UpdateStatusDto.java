package org.logitrack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.logitrack.enums.Status;

@Data
public class UpdateStatusDto {
    @NotEmpty(message = "Order ID is required")
    private Long orderId;
    @NotNull(message = "Status is required")
    private Status newStatus;
}
