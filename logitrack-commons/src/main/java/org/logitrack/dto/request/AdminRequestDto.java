package org.logitrack.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdminRequestDto {
    @NotBlank(message = "Cannot be blank or empty")
    private Long orderId;

    @NotBlank(message = "Cannot be blank or empty")
    private Long deliveryManId;
}
