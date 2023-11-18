package org.logitrack.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Random;

@Entity
@Table(name = "verifcation_tokens")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    private LocalDateTime expirationTime;

    private String confirmationToken;
    private String email;
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_Id")
    private User user;

    public VerificationToken(User user) {
        this.user = user;
        this.email = user.getEmail();
        createdDate = new Date();
        this.expirationTime = LocalDateTime.now().plusSeconds(300);
        confirmationToken = generateNumericOTP();
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
