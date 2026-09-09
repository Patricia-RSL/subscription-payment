package com.patricia.subscriptionApp.service.impl;

import com.patricia.subscriptionApp.entity.Subscription;
import com.patricia.subscriptionApp.enums.SubscriptionStatus;
import com.patricia.subscriptionApp.repository.SubscriptionRepository;
import com.patricia.subscriptionApp.service.PendingCancelationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CancellationServiceImpl implements PendingCancelationService {

    private final SubscriptionRepository subscriptionRepository;

    @Transactional
    @Override
    public void finalizePendingCancellations() {
        List<Subscription> pending = subscriptionRepository.findByStatusAndDataExpiracaoLessThanEqualForUpdate(SubscriptionStatus.CANCELADA_PENDENTE, LocalDate.now());

        for (Subscription s : pending) {
            s.setStatus(SubscriptionStatus.CANCELADA);
            subscriptionRepository.save(s);
            log.info("Finalized cancellation for subscription={}", s.getId());
        }
    }
}
