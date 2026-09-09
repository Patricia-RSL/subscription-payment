package subscriptionApp.service;

import com.patricia.subscriptionApp.entity.Subscription;
import com.patricia.subscriptionApp.entity.User;
import com.patricia.subscriptionApp.enums.PlanType;
import com.patricia.subscriptionApp.enums.SubscriptionStatus;
import com.patricia.subscriptionApp.repository.SubscriptionRepository;
import com.patricia.subscriptionApp.service.PaymentService;
import com.patricia.subscriptionApp.service.impl.RenewalServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RenewalServiceImplTest {

    @Test
    void renewSubscription_shouldExpireOldCreateNewAndRequestPayment() {
        SubscriptionRepository repo = mock(SubscriptionRepository.class);
        PaymentService paymentService = mock(PaymentService.class);
        RenewalServiceImpl service = new RenewalServiceImpl(repo, paymentService);

        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).email("a@b.com").build();
        LocalDate oldExpiration = LocalDate.now();

        Subscription actual = Subscription.builder()
                .user(user)
                .status(SubscriptionStatus.ATIVA)
                .plano(PlanType.BASICO)
                .dataInicio(oldExpiration.minusMonths(1))
                .dataExpiracao(oldExpiration)
                .build();

        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(repo.findByUser_IdAndStatusForUpdate(userId, SubscriptionStatus.PRE_PROCESSADA))
                .thenReturn(Optional.empty());

        service.renewSubscription(actual);

        // assinatura antiga expirada
        assertEquals(SubscriptionStatus.EXPIRADA, actual.getStatus());

        // nova assinatura criada corretamente e pagamento solicitado
        ArgumentCaptor<Subscription> newSubCap = ArgumentCaptor.forClass(Subscription.class);
        verify(repo, times(2)).save(newSubCap.capture());
        // duas chamadas: 1) atual expirada 2) nova ATIVA
        Subscription newSub = newSubCap.getAllValues().get(1);
        assertEquals(SubscriptionStatus.ATIVA, newSub.getStatus());
        assertEquals(userId, newSub.getUser().getId());
        assertEquals(PlanType.BASICO, newSub.getPlano());
        assertEquals(oldExpiration, newSub.getDataInicio());
        assertEquals(oldExpiration.plusMonths(1), newSub.getDataExpiracao());

        verify(paymentService).createNewPaymentAndSendRequest(newSub);
        verify(repo).findByUser_IdAndStatusForUpdate(userId, SubscriptionStatus.PRE_PROCESSADA);
    }
}
