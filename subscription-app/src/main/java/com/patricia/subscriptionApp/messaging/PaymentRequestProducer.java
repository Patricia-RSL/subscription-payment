package com.patricia.subscriptionApp.messaging;

import com.patricia.subscriptionApp.config.AppConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentRequestProducer {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Retryable(
        value = { RuntimeException.class, AmqpException.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 2000)
    )
    public void sendPaymentRequest(Map<String, Object> paymentRequest) {
        try {
            String message = objectMapper.writeValueAsString(paymentRequest);
            rabbitTemplate.convertAndSend(AppConfig.PAYMENT_REQUEST_QUEUE, message);
            log.info("Sent payment request message: {}", message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize payment request message", e);
        }
    }

    @Recover
    public void recover(Exception e, Map<String, Object> paymentRequest) {
        log.error("Failed to send payment request after retries: {}", paymentRequest, e);
        try {
            String message = objectMapper.writeValueAsString(paymentRequest);
            rabbitTemplate.convertAndSend(AppConfig.PAYMENT_REQUEST_DLQ, message);
            log.info("Sent failed payment request message to DLQ: {}", message);
        } catch (JsonProcessingException | AmqpException ex) {
            log.error("Failed to send payment request to DLQ: {}", paymentRequest, ex);
        }
    }
}
