package subscriptionApp.scheduler;

import com.patricia.subscriptionApp.scheduler.CancelPendingScheduler;
import com.patricia.subscriptionApp.service.PendingCancelationService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class CancelPendingSchedulerTest {

    @Test
    void finalizePendingCancellations_shouldDelegate() {
        PendingCancelationService service = mock(PendingCancelationService.class);
        CancelPendingScheduler scheduler = new CancelPendingScheduler(service);
        scheduler.finalizePendingCancellations();
        verify(service).finalizePendingCancellations();
    }
}

