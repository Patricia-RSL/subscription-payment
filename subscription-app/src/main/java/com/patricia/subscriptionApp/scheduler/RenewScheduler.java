package com.patricia.subscriptionApp.scheduler;

import com.patricia.subscriptionApp.entity.Subscription;
import com.patricia.subscriptionApp.enums.SubscriptionStatus;
import com.patricia.subscriptionApp.repository.SubscriptionRepository;
import com.patricia.subscriptionApp.service.RenewalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RenewScheduler {

    private final RenewalService renewalService;
    private final SubscriptionRepository repository;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void renewAllActiveSubscriptions() {
        log.info("Renewing all active subscriptions");

        List<Subscription> activeSubscriptions = repository.findByStatusAndDataExpiracaoLessThanEqualForUpdate(SubscriptionStatus.ATIVA, LocalDate.now());

        for (Subscription actualSubscription : activeSubscriptions) {
            try {
                renewalService.renewSubscription(actualSubscription);
            } catch (Exception e) {
                log.error("Error renewing subscription {}", actualSubscription.getId(), e);
            }
        }
    }

}
