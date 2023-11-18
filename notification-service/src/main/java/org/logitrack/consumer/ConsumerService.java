package org.logitrack.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.logitrack.dto.MessagingObject;
import org.logitrack.smtp.MailClient;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsumerService {
    private final MailClient mailClient;
    private final ObjectMapper mapper;

    @RabbitListener(queues = "${order-status-queue}")
    public void listenOrderStatus(String in) throws JsonProcessingException {
        log.info("Message received from order-status-queue: " + in);
        MessagingObject messagingObject = mapper.readValue(in, MessagingObject.class);
        mailClient.sendMessage(messagingObject);
        log.info("Email sent successfully for order status");
    }
    @RabbitListener(queues = "${order-created-queue}")
    public void listenOrderCreated(String in) throws JsonProcessingException {
        log.info("Message received from order-created-queue: " + in);
        MessagingObject messagingObject = mapper.readValue(in, MessagingObject.class);
        mailClient.sendMessage(messagingObject);
        log.info("Email sent successfully for order created");
    }
    @RabbitListener(queues = "${user-registration-queue}")
    public void listenUserRegistration(String in) throws JsonProcessingException {
        log.info("Message received from user-registration-queue: " + in);
        MessagingObject messagingObject = mapper.readValue(in, MessagingObject.class);
        mailClient.sendMessage(messagingObject);
        log.info("Email sent successfully for user registration");
    }
    @RabbitListener(queues = "${assigned-order-queue}")
    public void listenAssignedOrder(String in) throws JsonProcessingException {
        log.info("Message received from assigned-order-queue: " + in);
        MessagingObject messagingObject = mapper.readValue(in, MessagingObject.class);
        mailClient.sendMessage(messagingObject);
        log.info("Email sent successfully for assigned order");
    }
    @RabbitListener(queues = "${delivery-man-creation-queue}")
    public void listenDeliveryManCreation(String in) throws JsonProcessingException {
        log.info("Message received from delivery-man-creation-queue: " + in);
        MessagingObject messagingObject = mapper.readValue(in, MessagingObject.class);
        mailClient.sendMessage(messagingObject);
        log.info("Email sent successfully for delivery man creation");
    }
}



