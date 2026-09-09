package subscriptionApp.service;

import com.patricia.subscriptionApp.dto.SubscriptionDto;
import com.patricia.subscriptionApp.entity.Subscription;
import com.patricia.subscriptionApp.entity.User;
import com.patricia.subscriptionApp.enums.PlanType;
import com.patricia.subscriptionApp.enums.SubscriptionStatus;
import com.patricia.subscriptionApp.exception.BadRequestException;
import com.patricia.subscriptionApp.exception.ResourceNotFoundException;
import com.patricia.subscriptionApp.repository.SubscriptionRepository;
import com.patricia.subscriptionApp.repository.UserRepository;
import com.patricia.subscriptionApp.service.PaymentService;
import com.patricia.subscriptionApp.service.impl.SubscriptionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SubscriptionServiceImplTest {

    @Mock
    private SubscriptionRepository repository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private SubscriptionServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create_shouldValidateAndPersist() {
        UUID userId = UUID.randomUUID();
        SubscriptionDto dto = SubscriptionDto.builder()
                .userId(userId)
                .plano(PlanType.PREMIUM)
                .build();

        when(userRepository.findByIdForUpdate(userId)).thenReturn(Optional.of(User.builder().id(userId).email("a@b.com").build()));
        when(repository.existsByUser_IdAndStatus(userId, SubscriptionStatus.ATIVA)).thenReturn(false);
        when(repository.save(any())).thenAnswer(inv -> {
            Subscription s = inv.getArgument(0);
            s.setId(UUID.randomUUID());
            return s;
        });

        Subscription saved = service.create(dto);

        assertNotNull(saved.getId());
        assertEquals(SubscriptionStatus.ATIVA, saved.getStatus());
        assertEquals(LocalDate.now(), saved.getDataInicio());
        assertEquals(LocalDate.now().plusMonths(1), saved.getDataExpiracao());
        verify(repository).save(any());
        verify(paymentService).createNewPaymentAndSendRequest(saved);
    }

    @Test
    void create_whenUserMissing_shouldThrow() {
        SubscriptionDto dto = SubscriptionDto.builder()
                .plano(PlanType.PREMIUM)
                .build();
        assertThrows(BadRequestException.class, () -> service.create(dto));
    }

    @Test
    void findById_whenNotFound_shouldThrowResourceNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.findById(id));
    }

    @Test
    void cancel_shouldSetStatusAccordingToExpiration() {
        UUID id = UUID.randomUUID();
        Subscription sub = Subscription.builder()
                .id(id)
                .status(SubscriptionStatus.ATIVA)
                .dataExpiracao(LocalDate.now().minusDays(1))
                .build();
        when(repository.findById(id)).thenReturn(Optional.of(sub));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Subscription updated = service.cancel(id);
        assertEquals(SubscriptionStatus.CANCELADA, updated.getStatus());
        verify(repository).save(any());
    }

    @Test
    void findAllPaginated_shouldValidateParams() {
        assertThrows(BadRequestException.class, () -> service.findAllPaginated(-1, 10, "id", "ASC"));
        assertThrows(BadRequestException.class, () -> service.findAllPaginated(0, 0, "id", "ASC"));
    }
}
