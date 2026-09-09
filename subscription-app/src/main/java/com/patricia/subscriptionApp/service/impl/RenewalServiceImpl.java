package com.patricia.subscriptionApp.service.impl;

import com.patricia.subscriptionApp.entity.Subscription;
import com.patricia.subscriptionApp.enums.SubscriptionStatus;
import com.patricia.subscriptionApp.repository.SubscriptionRepository;
import com.patricia.subscriptionApp.service.PaymentService;
import com.patricia.subscriptionApp.service.RenewalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RenewalServiceImpl implements RenewalService {

    private final SubscriptionRepository repository;
    private final PaymentService paymentService;

    @Override
    @Transactional
    public void renewSubscription(Subscription actualSubscription) {

        actualSubscription.setStatus(SubscriptionStatus.EXPIRADA);
        repository.save(actualSubscription);

        Optional<Subscription> preProcessed = repository.findByUser_IdAndStatusForUpdate(
                actualSubscription.getUser().getId(),
                SubscriptionStatus.PRE_PROCESSADA
        );

        if(preProcessed.isPresent()) {
            log.info("User {} already has a pre-processed subscription. Skipping renewal.", actualSubscription.getUser().getId());

            Subscription newSub = preProcessed.get();
            newSub.setStatus(SubscriptionStatus.ATIVA);
            repository.save(newSub);
            paymentService.createNewPaymentAndSendRequest(newSub);

            return;
        }

        LocalDate oldExpirationDate = actualSubscription.getDataExpiracao();

        Subscription newSub = Subscription.builder()
                .user(actualSubscription.getUser())
                .plano(actualSubscription.getPlano())
                .status(SubscriptionStatus.ATIVA)
                .dataInicio(oldExpirationDate)
                .dataExpiracao(oldExpirationDate.plusMonths(1))
                .build();
        repository.save(newSub);

        paymentService.createNewPaymentAndSendRequest(newSub);
    }
}
