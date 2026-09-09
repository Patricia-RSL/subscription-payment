package com.patricia.subscriptionApp.messaging;

import com.patricia.subscriptionApp.config.AppConfig;
import com.patricia.subscriptionApp.enums.PaymentStatus;
import com.patricia.subscriptionApp.event.PaymentProcessedEvent;
import com.patricia.subscriptionApp.listener.PaymentProcessedListener;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentResultConsumer {

    private final PaymentProcessedListener listener;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = AppConfig.PAYMENT_RESULT_QUEUE)
    @Transactional
    public void receive(String payload) throws JsonProcessingException {
        try {
            if (payload != null && payload.contains("FORCE_ERROR")) {
                log.warn("[RETRY TEST] 'FORCE_ERROR' keyword detected. Simulating a transient infrastructure failure...");
                throw new RuntimeException("Simulated connection timeout to downstream gateway.");
            }

            log.info("Received payment result message: {}", payload);
            Map<String, Object> msg = objectMapper.readValue(payload, new TypeReference<Map<String, Object>>() {});
            String paymentIdStr = (String) msg.get("paymentId");
            String statusStr = (String) msg.get("status");

            UUID paymentId = UUID.fromString(paymentIdStr);
            PaymentStatus status = PaymentStatus.valueOf(statusStr);

            PaymentProcessedEvent event = new PaymentProcessedEvent(paymentId, status);
            listener.handle(event);

        } catch (Exception e) {
            log.error("Error processing payment result message", e);
            throw e;
        }
    }
}
