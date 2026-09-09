package com.patricia.paymentSimulator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.patricia.paymentSimulator.PaymentSimulatorConfig.PAYMENT_REQUEST_QUEUE;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentRequestListener {

    private final PaymentSimulatorService simulatorService;
    private final ObjectMapper objectMapper;

    @RabbitListener(id = "paymentListener", queues = PAYMENT_REQUEST_QUEUE)
    public void receive(Message message) throws Exception {
        String payload = new String(message.getBody());
        String correlationId = message.getMessageProperties().getCorrelationId();
        String paymentId = null;

        try {
            Map<String, Object> paymentRequest = objectMapper.readValue(payload, new TypeReference<Map<String, Object>>() {
            });
            paymentId = paymentRequest.get("paymentId") != null ? paymentRequest.get("paymentId").toString() : null;
            log.info("Processing payment request. correlationId={}, paymentId={}, payload={}", correlationId, paymentId, paymentRequest);

            Map<String, Object> result = simulatorService.processPaymentRequest(paymentRequest);
            log.info("Published payment result. correlationId={}, paymentId={}, result={}", correlationId, paymentId, result);

        } catch (Exception e) {
            log.error("Error processing payment request. correlationId={}, paymentId={}, payload={}", correlationId, paymentId, payload, e);
            throw e;
        }
    }
}
