package subscriptionApp.service;

import com.patricia.subscriptionApp.entity.Payment;
import com.patricia.subscriptionApp.entity.Subscription;
import com.patricia.subscriptionApp.entity.User;
import com.patricia.subscriptionApp.enums.PaymentStatus;
import com.patricia.subscriptionApp.enums.PlanType;
import com.patricia.subscriptionApp.messaging.PaymentRequestProducer;
import com.patricia.subscriptionApp.repository.PaymentRepository;
import com.patricia.subscriptionApp.repository.SubscriptionRepository;
import com.patricia.subscriptionApp.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private PaymentRequestProducer paymentRequestProducer;
    // Not used by service, kept to assert no direct interactions
    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private PaymentServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void requestPayment_whenMaxAttempts_shouldFailAndSuspend() {
        Subscription sub = Subscription.builder().id(UUID.randomUUID()).build();
        Payment p = Payment.builder()
                .id(UUID.randomUUID())
                .status(PaymentStatus.PENDING)
                .attemptCount(3)
                .subscription(sub)
                .build();

        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(subscriptionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.requestPayment(p);

        assertEquals(PaymentStatus.FAILED, p.getStatus());
        assertEquals(com.patricia.subscriptionApp.enums.SubscriptionStatus.SUSPENSA, sub.getStatus());
        verify(paymentRepository).save(p);
        verify(subscriptionRepository).save(sub);
        verifyNoInteractions(rabbitTemplate);
        verifyNoInteractions(paymentRequestProducer);
    }

    @Test
    void requestPayment_whenBelowMax_shouldIncrementAndSendRequest() {
        Subscription sub = Subscription.builder()
                .id(UUID.randomUUID())
                .user(User.builder().id(UUID.randomUUID()).email("a@b.com").build())
                .plano(PlanType.BASICO)
                .build();
        Payment p = Payment.builder()
                .id(UUID.randomUUID())
                .status(PaymentStatus.PENDING)
                .attemptCount(1)
                .subscription(sub)
                .createdAt(LocalDateTime.now())
                .build();

        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Ativa sincronização transacional para capturar o afterCommit
        boolean startedSync = false;
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.initSynchronization();
            startedSync = true;
        }
        try {
            service.requestPayment(p);
            assertEquals(2, p.getAttemptCount());
            verify(paymentRepository).save(p);

            // Dispara callbacks afterCommit registrados em sendPaymentRequest
            List<TransactionSynchronization> syncs = TransactionSynchronizationManager.getSynchronizations();
            syncs.forEach(TransactionSynchronization::afterCommit);

            verify(paymentRequestProducer).sendPaymentRequest(anyMap());
            verifyNoInteractions(rabbitTemplate);
        } finally {
            if (startedSync) TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void sendPaymentRequest_shouldRegisterAfterCommitPublisher() {
        Subscription sub = Subscription.builder()
                .id(UUID.randomUUID())
                .user(User.builder().id(UUID.randomUUID()).email("a@b.com").build())
                .plano(PlanType.BASICO)
                .dataInicio(LocalDate.now())
                .dataExpiracao(LocalDate.now().plusDays(10))
                .build();
        Payment p = Payment.builder()
                .id(UUID.randomUUID())
                .subscription(sub)
                .status(PaymentStatus.PENDING)
                .attemptCount(1)
                .createdAt(LocalDateTime.now())
                .build();

        boolean startedSync = false;
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.initSynchronization();
            startedSync = true;
        }
        try {
            service.sendPaymentRequest(p);
            TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
            verify(paymentRequestProducer).sendPaymentRequest(anyMap());
            verifyNoInteractions(rabbitTemplate);
        } finally {
            if (startedSync) TransactionSynchronizationManager.clearSynchronization();
        }
    }
}
