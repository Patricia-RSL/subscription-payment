package com.patricia.subscriptionApp.scheduler;

import com.patricia.subscriptionApp.service.PendingCancelationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class CancelPendingScheduler {

    private final PendingCancelationService pendingCancelationService;

    @Scheduled(cron = "0 0 4 * * *") // daily at 04:00
    @Transactional
    public void finalizePendingCancellations() {
        log.info("Running cancel pending scheduler");
        pendingCancelationService.finalizePendingCancellations();
    }
}

