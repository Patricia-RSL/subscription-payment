package paymentsimulator;

import com.patricia.paymentSimulator.PaymentSimulatorService;
import com.patricia.paymentSimulator.SimulatorController;
import com.patricia.paymentSimulator.SimulatorState;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SimulatorControllerTest {

    @Test
    void processSuccess_shouldReturnOkWithResult() throws Exception {
        PaymentSimulatorService service = mock(PaymentSimulatorService.class);
        SimulatorState state = new SimulatorState();
        RabbitListenerEndpointRegistry registry = mock(RabbitListenerEndpointRegistry.class);
        SimulatorController controller = new SimulatorController(service, state, registry);

        UUID id = UUID.randomUUID();
        Map<String, Object> expected = Map.of("paymentId", id.toString(), "status", "SUCCESS");
        doReturn(ResponseEntity.ok(expected)).when(service).processWithStatus(id, "SUCCESS");

        ResponseEntity<?> resp = controller.processSuccess(id);
        assertEquals(200, resp.getStatusCode().value());
        assertEquals(expected, resp.getBody());
    }

    @Test
    void processFail_shouldReturnOkWithResult() throws Exception {
        PaymentSimulatorService service = mock(PaymentSimulatorService.class);
        SimulatorState state = new SimulatorState();
        RabbitListenerEndpointRegistry registry = mock(RabbitListenerEndpointRegistry.class);
        SimulatorController controller = new SimulatorController(service, state, registry);

        UUID id = UUID.randomUUID();
        Map<String, Object> expected = Map.of("paymentId", id.toString(), "status", "FAILED");
        doReturn(ResponseEntity.ok(expected)).when(service).processWithStatus(id, "FAILED");

        ResponseEntity<?> resp = controller.processFail(id);
        assertEquals(200, resp.getStatusCode().value());
        assertEquals(expected, resp.getBody());
    }

    @Test
    void enableAutoConsume_shouldStartContainerAndReturnMessage() {
        PaymentSimulatorService service = mock(PaymentSimulatorService.class);
        SimulatorState state = new SimulatorState();
        RabbitListenerEndpointRegistry registry = mock(RabbitListenerEndpointRegistry.class);
        MessageListenerContainer container = mock(MessageListenerContainer.class);
        when(registry.getListenerContainer("paymentListener")).thenReturn(container);

        SimulatorController controller = new SimulatorController(service, state, registry);

        ResponseEntity<String> resp = controller.enableAutoConsume();

        verify(container).start();
        assertTrue(state.isAutoConsume());
        assertEquals(200, resp.getStatusCode().value());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().contains("Auto-consume ligado"));
    }

    @Test
    void disableAutoConsume_shouldStopContainerAndReturnMessage() {
        PaymentSimulatorService service = mock(PaymentSimulatorService.class);
        SimulatorState state = new SimulatorState();
        RabbitListenerEndpointRegistry registry = mock(RabbitListenerEndpointRegistry.class);
        MessageListenerContainer container = mock(MessageListenerContainer.class);
        when(registry.getListenerContainer("paymentListener")).thenReturn(container);

        SimulatorController controller = new SimulatorController(service, state, registry);

        ResponseEntity<?> resp = controller.disableAutoConsume();

        verify(container).stop();
        assertFalse(state.isAutoConsume());
        assertEquals(200, resp.getStatusCode().value());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().toString().contains("Auto-consume desligado"));
    }

    @Test
    void enableAutoConsume_whenContainerMissing_shouldReturnNotFoundMessage() {
        PaymentSimulatorService service = mock(PaymentSimulatorService.class);
        SimulatorState state = new SimulatorState();
        RabbitListenerEndpointRegistry registry = mock(RabbitListenerEndpointRegistry.class);
        when(registry.getListenerContainer("paymentListener")).thenReturn(null);

        SimulatorController controller = new SimulatorController(service, state, registry);
        ResponseEntity<String> resp = controller.enableAutoConsume();

        assertEquals(200, resp.getStatusCode().value());
        assertEquals("Listener não encontrado", resp.getBody());
    }
}
