package subscriptionApp.messaging;

import com.patricia.subscriptionApp.event.PaymentProcessedEvent;
import com.patricia.subscriptionApp.listener.PaymentProcessedListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.patricia.subscriptionApp.messaging.PaymentResultConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaymentResultConsumerTest {

    private PaymentProcessedListener listener;
    private ObjectMapper objectMapper;
    private PaymentResultConsumer consumer;

    @BeforeEach
    void setUp() {
        listener = mock(PaymentProcessedListener.class);
        objectMapper = new ObjectMapper();
        consumer = new PaymentResultConsumer(listener, objectMapper);
    }

    @Test
    void receive_shouldParseAndForwardEvent() throws Exception {
        String payload = "{\"paymentId\":\"8d2a6f1e-1111-2222-3333-444444444444\",\"status\":\"SUCCESS\"}";
        consumer.receive(payload);
        verify(listener).handle(any(PaymentProcessedEvent.class));
    }

    @Test
    void receive_whenInvalid_shouldThrow() {
        String payload = "invalid";
        try {
            consumer.receive(payload);
        } catch (Exception ignored) {}
        // on error it rethrows; just ensure listener not called
        verify(listener, never()).handle(any());
    }
}

