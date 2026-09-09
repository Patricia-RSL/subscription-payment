package subscriptionApp.scheduler;

import com.patricia.subscriptionApp.entity.Subscription;
import com.patricia.subscriptionApp.repository.SubscriptionRepository;
import com.patricia.subscriptionApp.scheduler.RenewScheduler;
import com.patricia.subscriptionApp.service.RenewalService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;

class RenewSchedulerTest {

    @Test
    void renewAllActiveSubscriptions_shouldCallServiceForEach() {
        RenewalService renewal = mock(RenewalService.class);
        SubscriptionRepository repo = mock(SubscriptionRepository.class);
        RenewScheduler scheduler = new RenewScheduler(renewal, repo);

        Subscription s1 = Subscription.builder().dataExpiracao(LocalDate.now()).build();
        Subscription s2 = Subscription.builder().dataExpiracao(LocalDate.now()).build();
        when(repo.findByStatusAndDataExpiracaoLessThanEqualForUpdate(any(), any())).thenReturn(List.of(s1, s2));

        scheduler.renewAllActiveSubscriptions();

        verify(renewal).renewSubscription(s1);
        verify(renewal).renewSubscription(s2);
    }
}

