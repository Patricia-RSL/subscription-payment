package subscriptionApp.listener;

import com.patricia.subscriptionApp.entity.Payment;
import com.patricia.subscriptionApp.entity.Subscription;
import com.patricia.subscriptionApp.enums.PaymentStatus;
import com.patricia.subscriptionApp.enums.SubscriptionStatus;
import com.patricia.subscriptionApp.event.PaymentProcessedEvent;
import com.patricia.subscriptionApp.listener.PaymentProcessedListener;
import com.patricia.subscriptionApp.repository.PaymentRepository;
import com.patricia.subscriptionApp.repository.SubscriptionRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PaymentProcessedListenerTest {

    @Test
    void handle_success_shouldUpdatePaymentAndSubscription() {
        PaymentRepository paymentRepo = mock(PaymentRepository.class);
        SubscriptionRepository subRepo = mock(SubscriptionRepository.class);
        PaymentProcessedListener listener = new PaymentProcessedListener(paymentRepo, subRepo);

        Subscription subscription = Subscription.builder()
                .id(UUID.randomUUID())
                .status(SubscriptionStatus.SUSPENSA)
                .dataInicio(LocalDate.now())
                .dataExpiracao(LocalDate.now().plusDays(10))
                .build();

        Payment payment = Payment.builder()
                .id(UUID.randomUUID())
                .subscription(subscription)
                .status(PaymentStatus.PENDING)
                .attemptCount(1)
                .createdAt(LocalDateTime.now())
                .build();

        when(paymentRepo.findById(payment.getId())).thenReturn(Optional.of(payment));

        listener.handle(new PaymentProcessedEvent(payment.getId(), PaymentStatus.SUCCESS));

        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
        verify(paymentRepo).saveAndFlush(payment);
        assertEquals(SubscriptionStatus.ATIVA, subscription.getStatus());
        verify(subRepo).saveAndFlush(subscription);
    }

    @Test
    void handle_whenPaymentAlreadySuccess_shouldReturnEarly() {
        PaymentRepository paymentRepo = mock(PaymentRepository.class);
        SubscriptionRepository subRepo = mock(SubscriptionRepository.class);
        PaymentProcessedListener listener = new PaymentProcessedListener(paymentRepo, subRepo);

        Payment payment = Payment.builder().id(UUID.randomUUID()).status(PaymentStatus.SUCCESS).build();
        when(paymentRepo.findById(payment.getId())).thenReturn(Optional.of(payment));

        listener.handle(new PaymentProcessedEvent(payment.getId(), PaymentStatus.SUCCESS));

        verify(paymentRepo, never()).saveAndFlush(any());
        verify(subRepo, never()).saveAndFlush(any());
    }
}

