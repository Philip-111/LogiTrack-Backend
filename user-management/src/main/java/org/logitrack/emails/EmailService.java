package org.logitrack.emails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.logitrack.dto.response.ApiResponse;
import org.logitrack.entities.User;
import org.logitrack.entities.VerificationToken;
import org.logitrack.repository.TokenRepository;
import org.logitrack.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender javaMailSender;
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;

    @Async
    public void sendEmail(SimpleMailMessage email) {
        javaMailSender.send(email);
    }

    public ApiResponse confirmEmail(String confirmationToken) {
        log.info("Trying to verify email");

        Optional<VerificationToken> veritoken = tokenRepository.findByConfirmationToken(confirmationToken);
        log.info("Verification token found in the database");

        if (veritoken.isPresent()) {
            VerificationToken newToken = veritoken.get();

            log.info("verification token retrieved from database" + newToken);
            // Check if the token has expired
            if (newToken.getExpirationTime().isBefore(LocalDateTime.now())) {
                log.info("token has expired");
                return new ApiResponse("10", "Token has expired",HttpStatus.UNAUTHORIZED,"failure");
            }

            Optional<User> tokuser = userRepository.findByEmail(newToken.getUser().getEmail());
            log.info("User associated with token is present");

            if (tokuser.isPresent()) {
                User realUser = tokuser.get();
                log.info("User retrieved");
                realUser.setIsVerified(true);
                log.info("User is verified");
                userRepository.save(realUser);
                return new ApiResponse("00", "OTP verified successfully", HttpStatus.OK, "success");
            } else {
                return new ApiResponse("OO", "User not in database", HttpStatus.OK, "success");
            }
        } else {
            return new ApiResponse("00", "The provided confirmation token is invalid or expired!",HttpStatus.OK, "success");
        }
    }


    @Async
    public void sendConfirmationEmail(User user, String confirmationLink) {

        String subject = "Email Verification";

        String senderName = "LogiTracker";

        String mailContent = "Hello, " +" \n"+ user.getFullName() + "\n" +
                "Thank you for choosing LogiTrack! We're excited to have you on board. To complete your registration, please use the one-time password (OTP) provided below:" +
                "\n" +
                "OTP: " + confirmationLink + " \nIf you didn't request this OTP, please disregard this message. Your security is important to us at:" +
                "\nAdminOne@gmail.com" + "\nThank you for trusting us" +
                "\nLogiTrack Team.";

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(user.getEmail());
        mailMessage.setSubject(subject);
        mailMessage.setFrom("logitrackapplication@gmail.com" + senderName);
        mailMessage.setText(mailContent);
        sendEmail(mailMessage);
    }

}