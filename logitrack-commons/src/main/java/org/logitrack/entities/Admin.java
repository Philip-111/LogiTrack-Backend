package org.logitrack.entities;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Admin extends User {
    private static  final long serialVersionUID=1l;
    private Integer assignmentsCompleted;
}
