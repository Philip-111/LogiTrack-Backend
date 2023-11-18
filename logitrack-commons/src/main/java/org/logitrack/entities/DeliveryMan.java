package org.logitrack.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@DiscriminatorValue("DeliveryMan")
public class DeliveryMan extends User {
    private static final long serialVersionUID=1l;
    private Integer ordersCompleted;
    private UUID deliveryManId = UUID.fromString(UUID.randomUUID().toString());
}
