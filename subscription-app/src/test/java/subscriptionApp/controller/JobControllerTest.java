package subscriptionApp.controller;

import com.patricia.subscriptionApp.controller.JobController;
import com.patricia.subscriptionApp.scheduler.CancelPendingScheduler;
import com.patricia.subscriptionApp.scheduler.PaymentScheduler;
import com.patricia.subscriptionApp.scheduler.RenewScheduler;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class JobControllerTest {

    @Test
    void endpoints_shouldDelegateToSchedulers() {
        RenewScheduler renew = mock(RenewScheduler.class);
        CancelPendingScheduler cancel = mock(CancelPendingScheduler.class);
        PaymentScheduler payment = mock(PaymentScheduler.class);

        JobController controller = new JobController(renew, cancel, payment);

        controller.renew();
        controller.cancelPending();
        controller.checkPendingPayments();

        verify(renew).renewAllActiveSubscriptions();
        verify(cancel).finalizePendingCancellations();
        verify(payment).requestPendingPayments();
    }
}

