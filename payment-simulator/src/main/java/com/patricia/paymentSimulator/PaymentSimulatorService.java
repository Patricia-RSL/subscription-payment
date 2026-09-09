package com.patricia.paymentSimulator;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service that centralizes publishing payment results and processing payment requests.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentSimulatorService {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Publish a result map as JSON to the payment result queue.
     */
    public void publishResult(Map<String, Object> result) throws Exception {
        String resultJson = objectMapper.writeValueAsString(result);
        rabbitTemplate.convertAndSend(PaymentSimulatorConfig.PAYMENT_RESULT_QUEUE, resultJson);
    }

    /**
     * Process a payment request map and publish a result with the specified status.
     * Returns the published result map.
     */
    public Map<String, Object> processPaymentRequestWithStatus(Map<String, Object> paymentRequest, String status) throws Exception {
        Map<String, Object> result = new HashMap<>();
        result.put("paymentId", paymentRequest.get("paymentId"));
        result.put("status", status);
        publishResult(result);
        log.info("Processed payment request {} with status {}", paymentRequest.get("paymentId"), status);
        return result;
    }

    /**
     * Process a payment request map and publish a result with random success or failure.
     * Returns the published result map.
     */
    public Map<String, Object> processPaymentRequest(Map<String, Object> paymentRequest) throws Exception {
        Map<String, Object> result = new HashMap<>();
        result.put("paymentId", paymentRequest.get("paymentId"));
        result.put("status", "SUCCESS");
        publishResult(result);
        log.info("Processed payment request {} with status {}", paymentRequest.get("paymentId"), result.get("status"));
        return result;
    }

    public ResponseEntity<?> processWithStatus(UUID uuid, String status) throws Exception {

        Map<String, Object> result =
                processPaymentRequestWithStatus(
                        Map.of("paymentId", uuid.toString()),
                        status
                );

        if (result == null) {
            throw new Exception("Message queue payment.request");
        }

        return ResponseEntity.ok(result);
    }

    public ResponseEntity<?> forceProcessWithError(UUID uuid) throws Exception {
        Map<String, Object> result =
                processPaymentRequestWithStatus(
                        Map.of("paymentId", "FORCE_ERROR"),
                        "SUCCESS"
                );

        if (result == null) {
            throw new Exception("Message queue payment.request");
        }

        return ResponseEntity.ok(result);
    }
}
