package org.logitrack.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${exchange-name}")
    private String exchangeName;

    @Value("${order-status-queue}")
    private String orderStatusQueueName;

    @Value("${order-created-queue}")
    private String orderCreatedQueueName;

    @Value("${user-registration-queue}")
    private String userRegistrationQueueName;

    @Value("${assigned-order-queue}")
    private String assignedOrderQueueName;

    @Value("${delivery-man-creation-queue}")
    private String deliveryManCreationQueueName;

    @Value("${order-status-routing-key}")
    private String orderStatusRoutingKey;

    @Value("${order-created-routing-key}")
    private String orderCreatedRoutingKey;

    @Value("${user-registration-routing-key}")
    private String userRegistrationRoutingKey;

    @Value("${assigned-order-routing-key}")
    private String assignedOrderRoutingKey;

    @Value("${delivery-man-creation-routing-key}")
    private String deliveryManCreationRoutingKey;

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(exchangeName);
    }

    @Bean
    public Queue orderStatusQueue() {
        return new Queue(orderStatusQueueName);
    }

    @Bean
    public Queue orderCreatedQueue() {
        return new Queue(orderCreatedQueueName);
    }

    @Bean
    public Queue userRegistrationQueue() {
        return new Queue(userRegistrationQueueName);
    }

    @Bean
    public Queue assignedOrderQueue() {
        return new Queue(assignedOrderQueueName);
    }

    @Bean
    public Queue deliveryManCreationQueue() {
        return new Queue(deliveryManCreationQueueName);
    }
    @Bean
    public Binding orderStatusBinding(Queue orderStatusQueue, DirectExchange exchange) {
        return BindingBuilder.bind(orderStatusQueue).to(exchange).with(orderStatusRoutingKey);
    }

    @Bean
    public Binding orderCreatedBinding(Queue orderCreatedQueue, DirectExchange exchange) {
        return BindingBuilder.bind(orderCreatedQueue).to(exchange).with(orderCreatedRoutingKey);
    }

    @Bean
    public Binding userRegistrationBinding(Queue userRegistrationQueue, DirectExchange exchange) {
        return BindingBuilder.bind(userRegistrationQueue).to(exchange).with(userRegistrationRoutingKey);
    }

    @Bean
    public Binding assignedOrderBinding(Queue assignedOrderQueue, DirectExchange exchange) {
        return BindingBuilder.bind(assignedOrderQueue).to(exchange).with(assignedOrderRoutingKey);
    }

    @Bean
    public Binding deliveryManCreationBinding(Queue deliveryManCreationQueue, DirectExchange exchange) {
        return BindingBuilder.bind(deliveryManCreationQueue).to(exchange).with(deliveryManCreationRoutingKey);
    }

    public RabbitTemplate rabbitTemplate() {
        return new RabbitTemplate();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
