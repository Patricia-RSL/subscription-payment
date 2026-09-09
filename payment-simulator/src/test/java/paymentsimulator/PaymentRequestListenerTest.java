package paymentsimulator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.patricia.paymentSimulator.PaymentRequestListener;
import com.patricia.paymentSimulator.PaymentSimulatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaymentRequestListenerTest {

    private PaymentSimulatorService simulatorService;
    private ObjectMapper objectMapper;

    private PaymentRequestListener listener;

    @BeforeEach
    void setUp() {
        simulatorService = mock(PaymentSimulatorService.class);
        objectMapper = spy(new ObjectMapper());
        listener = new PaymentRequestListener(simulatorService, objectMapper);
    }

    @Test
    void receive_shouldProcessAndPublish() throws Exception {
        String payload = "{\"paymentId\":\"abc\"}";
        Map<String, Object> expected = Map.of("paymentId", "abc", "status", "SUCCESS");
        when(simulatorService.processPaymentRequest(any())).thenReturn(expected);

        MessageProperties properties = new MessageProperties();
        properties.setCorrelationId("corr-123");
        Message message = new Message(payload.getBytes(StandardCharsets.UTF_8), properties);

        listener.receive(message);

        verify(simulatorService).processPaymentRequest(any());
        verify(simulatorService, never()).publishResult(any());
    }

    @Test
    void receive_whenInvalid_shouldThrow() throws Exception {
        String payload = "invalid-json";
        MessageProperties properties = new MessageProperties();
        properties.setCorrelationId("corr-456");
        Message message = new Message(payload.getBytes(StandardCharsets.UTF_8), properties);

        assertThrows(Exception.class, () -> listener.receive(message));
        verify(simulatorService, never()).processPaymentRequest(any());
        verify(simulatorService, never()).publishResult(any());
    }
}
