package org.logitrack.smtp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.logitrack.dto.MessagingObject;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
@Slf4j
@Service
@RequiredArgsConstructor
public class MailClient {
    private final JavaMailSender javaMailSender;

    @Async
    public void sendMessage(MessagingObject mailContent) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(mailContent.getDestinationEmail());

        switch (mailContent.getPurpose()) {
            case ORDER_STATUS -> mailMessage.setSubject("Order Status");
            case USER_REGISTRATION -> mailMessage.setSubject("User Created!");
            case ORDER_CREATED -> mailMessage.setSubject("Order Created");
            case ASSIGNED_ORDER -> mailMessage.setSubject("Assigned Order");
            case DELIVERY_MAN_CREATION -> mailMessage.setSubject("Delivery-Man Creation");
            default -> mailMessage.setSubject("New Notification from Logitrack!");
        }

        mailMessage.setFrom("logitrackapplication@gmail.com" + mailContent.getSenderName());
        mailMessage.setText(mailContent.getMessage());
        sendEmail(mailMessage);
    }
    @Async
    public void sendEmail(SimpleMailMessage email) {
        javaMailSender.send(email);
        log.info("email sent successfully");
    }
}



