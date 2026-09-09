package subscriptionApp.messaging;

import com.patricia.subscriptionApp.config.AppConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.patricia.subscriptionApp.messaging.PaymentRequestProducer;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentRequestProducerTest {

    @Test
    void sendPaymentRequest_shouldSerializeAndSend() throws Exception {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        PaymentRequestProducer producer = new PaymentRequestProducer(rabbitTemplate, objectMapper);

        Map<String, Object> req = Map.of("paymentId", "123", "amount", 10);
        String json = "{\"paymentId\":\"123\",\"amount\":10}";
        when(objectMapper.writeValueAsString(req)).thenReturn(json);

        producer.sendPaymentRequest(req);

        verify(rabbitTemplate).convertAndSend(eq(AppConfig.PAYMENT_REQUEST_QUEUE), eq(json));
    }

    @Test
    void sendPaymentRequest_whenSerializationFails_shouldNotSend() {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        PaymentRequestProducer producer = new PaymentRequestProducer(rabbitTemplate, objectMapper);

        Map<String, Object> req = Map.of("x", "y");
        try {
            when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("boom") {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        assertThrows(RuntimeException.class, () -> {
            producer.sendPaymentRequest(req);
        });

        verifyNoInteractions(rabbitTemplate);
    }
}
