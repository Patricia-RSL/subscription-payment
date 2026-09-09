package paymentsimulator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.patricia.paymentSimulator.PaymentSimulatorConfig;
import com.patricia.paymentSimulator.PaymentSimulatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class PaymentSimulatorServiceTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PaymentSimulatorService service;

    AutoCloseable openMocks;

    @BeforeEach
    void setUp() {
        openMocks = MockitoAnnotations.openMocks(this);
    }

    @Test
    void testProcessPaymentRequestWithStatus() throws Exception {
        Map<String, Object> paymentRequest = Map.of("paymentId", "123");
        String status = "SUCCESS";
        String expectedJson = "{\"paymentId\":\"123\",\"status\":\"SUCCESS\"}";

        when(objectMapper.writeValueAsString(any())).thenReturn(expectedJson);

        Map<String, Object> result = service.processPaymentRequestWithStatus(paymentRequest, status);

        assertEquals("123", result.get("paymentId"));
        assertEquals("SUCCESS", result.get("status"));

        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        verify(rabbitTemplate).convertAndSend(eq(PaymentSimulatorConfig.PAYMENT_RESULT_QUEUE), jsonCaptor.capture());
        assertEquals(expectedJson, jsonCaptor.getValue());
    }

    @Test
    void testProcessPaymentRequest() throws Exception {
        Map<String, Object> paymentRequest = Map.of("paymentId", "456");

        String expectedJson = "{\"paymentId\":\"456\",\"status\":\"SUCCESS\"}";
        when(objectMapper.writeValueAsString(any())).thenReturn(expectedJson);
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString());

        // Directly call processPaymentRequestWithStatus to avoid mocking Math.random
        Map<String, Object> result = service.processPaymentRequestWithStatus(paymentRequest, "SUCCESS");

        assertEquals("456", result.get("paymentId"));
        assertEquals("SUCCESS", result.get("status"));

        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        verify(rabbitTemplate).convertAndSend(eq(PaymentSimulatorConfig.PAYMENT_RESULT_QUEUE), jsonCaptor.capture());
        assertEquals(expectedJson, jsonCaptor.getValue());
    }

    @Test
    void testProcessPaymentRequestRandomStatus() throws Exception {
        Map<String, Object> paymentRequest = Map.of("paymentId", "789");

        String expectedJsonSuccess = "{\"paymentId\":\"789\",\"status\":\"SUCCESS\"}";
        String expectedJsonFailed = "{\"paymentId\":\"789\",\"status\":\"FAILED\"}";

        when(objectMapper.writeValueAsString(any())).thenAnswer(invocation -> {
            Map<String, Object> arg = invocation.getArgument(0);
            if ("SUCCESS".equals(arg.get("status"))) {
                return expectedJsonSuccess;
            } else if ("FAILED".equals(arg.get("status"))) {
                return expectedJsonFailed;
            }
            return "{}";
        });

        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString());

        // We cannot predict the random result, so just test both possibilities
        Map<String, Object> result = service.processPaymentRequest(paymentRequest);

        assertEquals("789", result.get("paymentId"));
        assertTrue("SUCCESS".equals(result.get("status")) || "FAILED".equals(result.get("status")));

        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        verify(rabbitTemplate).convertAndSend(eq(PaymentSimulatorConfig.PAYMENT_RESULT_QUEUE), jsonCaptor.capture());

        String sentJson = jsonCaptor.getValue();
        assertTrue(sentJson.equals(expectedJsonSuccess) || sentJson.equals(expectedJsonFailed));
    }
}
