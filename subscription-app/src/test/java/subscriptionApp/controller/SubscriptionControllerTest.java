package subscriptionApp.controller;

import com.patricia.subscriptionApp.controller.SubscriptionController;
import com.patricia.subscriptionApp.dto.SubscriptionDto;
import com.patricia.subscriptionApp.entity.Subscription;
import com.patricia.subscriptionApp.enums.PlanType;
import com.patricia.subscriptionApp.enums.SubscriptionStatus;
import com.patricia.subscriptionApp.service.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.patricia.subscriptionApp.exception.ResourceNotFoundException;

class SubscriptionControllerTest {

    @Test
    void getAll_shouldReturnMappedList() {
        SubscriptionService service = mock(SubscriptionService.class);
        SubscriptionController controller = new SubscriptionController(service);

        Subscription s1 = Subscription.builder()
                .id(UUID.randomUUID())
                .plano(PlanType.BASICO)
                .status(SubscriptionStatus.ATIVA)
                .dataInicio(LocalDate.now())
                .dataExpiracao(LocalDate.now().plusDays(7))
                .build();
        when(service.findAll()).thenReturn(List.of(s1));

        ResponseEntity<List<SubscriptionDto>> resp = controller.getAll();
        assertEquals(200, resp.getStatusCode().value());
        assertEquals(1, resp.getBody().size());
        assertEquals(s1.getId(), resp.getBody().get(0).getId());
    }

    @Test
    void getById_shouldReturnDto() {
        SubscriptionService service = mock(SubscriptionService.class);
        SubscriptionController controller = new SubscriptionController(service);
        UUID id = UUID.randomUUID();
        Subscription s = Subscription.builder()
                .id(id)
                .plano(PlanType.PREMIUM)
                .status(SubscriptionStatus.ATIVA)
                .dataInicio(LocalDate.now())
                .dataExpiracao(LocalDate.now().plusDays(10))
                .build();
        when(service.findById(id)).thenReturn(s);

        ResponseEntity<SubscriptionDto> resp = controller.getById(id);
        assertEquals(200, resp.getStatusCode().value());
        assertEquals(id, resp.getBody().getId());
    }

    @Test
    void create_shouldDelegateAndReturnCreated() {
        SubscriptionService service = mock(SubscriptionService.class);
        SubscriptionController controller = new SubscriptionController(service);
        SubscriptionDto dto = SubscriptionDto.builder()
                .plano(PlanType.BASICO)
                .build();
        Subscription created = Subscription.builder().id(UUID.randomUUID()).plano(PlanType.BASICO)
                .status(SubscriptionStatus.ATIVA)
                .dataInicio(LocalDate.now()).dataExpiracao(LocalDate.now().plusDays(7)).build();
        when(service.create(dto)).thenReturn(created);

        ResponseEntity<SubscriptionDto> resp = controller.create(dto);
        assertEquals(201, resp.getStatusCode().value());
        assertEquals(created.getId(), resp.getBody().getId());
    }

    @Test
    void cancel_shouldReturnUpdated() {
        SubscriptionService service = mock(SubscriptionService.class);
        SubscriptionController controller = new SubscriptionController(service);
        UUID id = UUID.randomUUID();
        Subscription updated = Subscription.builder().id(id).status(SubscriptionStatus.CANCELADA).plano(PlanType.BASICO)
                .dataInicio(LocalDate.now()).dataExpiracao(LocalDate.now()).build();
        when(service.cancel(id)).thenReturn(updated);

        ResponseEntity<SubscriptionDto> resp = controller.cancel(id);
        assertEquals(200, resp.getStatusCode().value());
        assertEquals(SubscriptionStatus.CANCELADA, resp.getBody().getStatus());
    }

    // New tests for endpoints in the excerpt

    @Test
    void getUserSubscriptionHistory_shouldReturnMappedListForGivenUser() {
        SubscriptionService service = mock(SubscriptionService.class);
        SubscriptionController controller = new SubscriptionController(service);
        UUID userId = UUID.randomUUID();

        Subscription s1 = Subscription.builder()
                .id(UUID.randomUUID())
                .plano(PlanType.BASICO)
                .status(SubscriptionStatus.ATIVA)
                .dataInicio(LocalDate.now())
                .dataExpiracao(LocalDate.now().plusDays(7))
                .build();
        when(service.findAllByUserId(userId)).thenReturn(List.of(s1));

        ResponseEntity<List<SubscriptionDto>> resp = controller.getUserSubscriptionHistory(userId);
        assertEquals(200, resp.getStatusCode().value());
        assertEquals(1, resp.getBody().size());
        assertEquals(s1.getId(), resp.getBody().get(0).getId());
        verify(service).findAllByUserId(any());
    }

    @Test
    void hasActiveSubscription_shouldReturnEntityWhenPresent() {
        SubscriptionService service = mock(SubscriptionService.class);
        SubscriptionController controller = new SubscriptionController(service);
        UUID userId = UUID.randomUUID();
        Subscription current = Subscription.builder()
                .id(UUID.randomUUID())
                .plano(PlanType.PREMIUM)
                .status(SubscriptionStatus.ATIVA)
                .dataInicio(LocalDate.now().minusDays(5))
                .dataExpiracao(LocalDate.now().plusDays(25))
                .build();
        when(service.findCurrentSubscription(userId)).thenReturn(java.util.Optional.of(current));

        ResponseEntity<SubscriptionDto> resp = controller.hasActiveSubscription(userId);
        assertEquals(200, resp.getStatusCode().value());
        assertEquals(current.getId(), resp.getBody().getId());
        verify(service).findCurrentSubscription(userId);
    }

    @Test
    void hasActiveSubscription_shouldThrowNotFoundWhenAbsent() {
        SubscriptionService service = mock(SubscriptionService.class);
        SubscriptionController controller = new SubscriptionController(service);
        UUID userId = UUID.randomUUID();
        when(service.findCurrentSubscription(userId)).thenReturn(java.util.Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> controller.hasActiveSubscription(userId));
        verify(service).findCurrentSubscription(userId);
    }

    @Test
    void update_shouldDelegateAndReturnOk() throws Exception {
        SubscriptionService service = mock(SubscriptionService.class);
        SubscriptionController controller = new SubscriptionController(service);
        UUID id = UUID.randomUUID();
        SubscriptionDto dto = SubscriptionDto.builder()
                .id(id)
                .plano(PlanType.BASICO)
                .build();
        Subscription updated = Subscription.builder()
                .id(id)
                .plano(PlanType.BASICO)
                .status(SubscriptionStatus.ATIVA)
                .dataInicio(LocalDate.now())
                .dataExpiracao(LocalDate.now().plusDays(7))
                .build();
        when(service.updatePlanType(dto)).thenReturn(updated);

        ResponseEntity<SubscriptionDto> resp = controller.update(dto);
        assertEquals(200, resp.getStatusCode().value());
        assertEquals(id, resp.getBody().getId());
        verify(service).updatePlanType(dto);
    }
}
