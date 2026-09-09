package subscriptionApp.scheduler;

import com.patricia.subscriptionApp.entity.Payment;
import com.patricia.subscriptionApp.enums.PaymentStatus;
import com.patricia.subscriptionApp.repository.PaymentRepository;
import com.patricia.subscriptionApp.scheduler.PaymentScheduler;
import com.patricia.subscriptionApp.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;

class PaymentSchedulerTest {

    @Test
    void requestPendingPayments_shouldProcessEachPendingPayment() {
        PaymentRepository repo = mock(PaymentRepository.class);
        PaymentServiceImpl service = mock(PaymentServiceImpl.class);
        PaymentScheduler scheduler = new PaymentScheduler(repo, service);

        Payment p1 = Payment.builder().status(PaymentStatus.PENDING).build();
        Payment p2 = Payment.builder().status(PaymentStatus.PENDING).build();
        when(repo.findByStatusOrderByCreatedAtAsc(PaymentStatus.PENDING)).thenReturn(List.of(p1, p2));

        scheduler.requestPendingPayments();

        verify(service).requestPayment(p1);
        verify(service).requestPayment(p2);
    }
}

