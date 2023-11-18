package org.logitrack.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.logitrack.enums.Gender;
import org.logitrack.enums.Role;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User  implements Serializable {

    private static  final long serialVersionUID=1l;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customId;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String email;

    //@Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String phoneNumber;

    private Gender gender;

    private String city;

    private String address;

    private String drivingLicenseNumber;

    private String State;

    private String stateOfIssue;

    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate birthday;


    @Column
    @Temporal(TemporalType.TIMESTAMP)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime lastLogin;

    @Column
    @CreationTimestamp
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime creationDate;

    @Column
    @UpdateTimestamp
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime updatedDate;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToOne
    @JoinColumn(name = "verification_token_id")
    private VerificationToken verificationToken;
    @Column
    private String uuid;

    @JsonIgnore
    @OneToMany(mappedBy ="user")
    private List<Transaction> transaction;

    private Boolean isVerified = false;
    @PostLoad
    public void generateCustomId() {
        String prefix = ("LT"+ generateNumericOTP() );
        //Long numericPart = id;
        customId = prefix; //+ numericPart;
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
}
