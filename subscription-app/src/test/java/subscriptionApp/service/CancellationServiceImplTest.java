package subscriptionApp.service;

import com.patricia.subscriptionApp.entity.Subscription;
import com.patricia.subscriptionApp.enums.SubscriptionStatus;
import com.patricia.subscriptionApp.repository.SubscriptionRepository;
import com.patricia.subscriptionApp.service.impl.CancellationServiceImpl;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CancellationServiceImplTest {

    @Test
    void finalizePendingCancellations_shouldUpdateEligibleOnes() {
        SubscriptionRepository repo = mock(SubscriptionRepository.class);
        CancellationServiceImpl service = new CancellationServiceImpl(repo);

        Subscription s1 = Subscription.builder().status(SubscriptionStatus.CANCELADA_PENDENTE).dataExpiracao(LocalDate.now().minusDays(1)).build();
        Subscription s2 = Subscription.builder().status(SubscriptionStatus.CANCELADA_PENDENTE).dataExpiracao(LocalDate.now().plusDays(1)).build();
        Subscription s3 = Subscription.builder().status(SubscriptionStatus.ATIVA).dataExpiracao(LocalDate.now()).build();
        when(repo.findByStatusAndDataExpiracaoLessThanEqualForUpdate(SubscriptionStatus.CANCELADA_PENDENTE, LocalDate.now())).thenReturn(List.of(s1));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.finalizePendingCancellations();

        assertEquals(SubscriptionStatus.CANCELADA, s1.getStatus());
        verify(repo, times(1)).save(s1);
        verify(repo, never()).save(s2);
        verify(repo, never()).save(s3);
    }
}
