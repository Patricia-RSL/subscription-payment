package com.patricia.subscriptionApp.service.impl;

import com.patricia.subscriptionApp.entity.Payment;
import com.patricia.subscriptionApp.entity.Subscription;
import com.patricia.subscriptionApp.enums.PaymentStatus;
import com.patricia.subscriptionApp.enums.PlanType;
import com.patricia.subscriptionApp.enums.SubscriptionStatus;
import com.patricia.subscriptionApp.exception.BadRequestException;
import com.patricia.subscriptionApp.messaging.PaymentRequestProducer;
import com.patricia.subscriptionApp.repository.PaymentRepository;
import com.patricia.subscriptionApp.repository.SubscriptionRepository;
import com.patricia.subscriptionApp.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRequestProducer paymentRequestProducer;

    @Override
    @Transactional
    public void requestPayment(Payment payment) {
        log.info("Checking payment {} with status {} and attempt count {}", payment.getId(), payment.getStatus(), payment.getAttemptCount());
        if (payment.getAttemptCount() >= 3) {

            log.info("Payment {} has reached maximum attempt count. Marking as FAILED and suspending subscription.", payment.getId());
            payment.setStatus(PaymentStatus.FAILED);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            Subscription subscription = payment.getSubscription();
            subscription.setStatus(SubscriptionStatus.SUSPENSA);
            subscriptionRepository.save(subscription);
            return ;
        }

        payment.setAttemptCount(payment.getAttemptCount() + 1);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        sendPaymentRequest(payment);
    }

    @Override
    @Transactional
    public void createNewPaymentAndSendRequest(Subscription actualSubscription) {
        try {
            Payment payment = Payment.builder()
                    .subscription(actualSubscription)
                    .amount(actualSubscription.getPlano().getValor())
                    .status(PaymentStatus.PENDING)
                    .attemptCount(1)
                    .createdAt(LocalDateTime.now())
                    .build();
            payment = paymentRepository.save(payment);
            sendPaymentRequest(payment);
        } catch (DataIntegrityViolationException e) {
            log.warn("Subscription {} already has a pending payment. Duplicate creation prevented by database constraint.", actualSubscription.getId());
        } catch (Exception e) {
            log.error("Unexpected error while creating payment for subscription {}: {}", actualSubscription.getId(), e.getMessage(), e);
        }
    }

    @Transactional
    @Override
    public void createPartialPaymentAndSendRequest(Subscription currentSubscription, Subscription newSubscription) {
        try {
            PlanType newPlan = newSubscription.getPlano();

            BigDecimal currentPlanValue = currentSubscription.getPlano().getValor();

            LocalDate today = LocalDate.now();
            LocalDate expirationDate = currentSubscription.getDataExpiracao();

            if (!expirationDate.isAfter(today)) {
                throw new BadRequestException(
                        "The subscription has already expired."
                );
            }

            long remainingDays = ChronoUnit.DAYS.between(today, expirationDate);
            long totalDays = ChronoUnit.DAYS.between(
                    currentSubscription.getDataInicio(),
                    expirationDate
            );

            BigDecimal currentRemainingValue = currentPlanValue
                    .divide(BigDecimal.valueOf(totalDays), 10, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(remainingDays));

            BigDecimal newRemainingValue = newPlan.getValor()
                    .divide(BigDecimal.valueOf(totalDays), 10, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(remainingDays));

            BigDecimal upgradeValue = newRemainingValue
                    .subtract(currentRemainingValue)
                    .setScale(2, RoundingMode.HALF_UP);

            log.info("Subscription upgrade: subscriptionId={}, remainingDays={}, " +
                            "currentRemainingValue={}, newRemainingValue={}, upgradeValue={}",
                    currentSubscription.getId(),remainingDays,currentRemainingValue,newRemainingValue,upgradeValue);

            Payment payment = Payment.builder()
                    .subscription(newSubscription)
                    .amount(upgradeValue)
                    .status(PaymentStatus.PENDING)
                    .attemptCount(1)
                    .createdAt(LocalDateTime.now())
                    .build();
            payment = paymentRepository.save(payment);
            sendPaymentRequest(payment);

        } catch (DataIntegrityViolationException e) {
            log.warn("Subscription {} already has a pending payment. Duplicate creation prevented by database constraint.", currentSubscription.getId());
        } catch (Exception e) {
            log.error("Unexpected error while creating payment for subscription {}: {}", currentSubscription.getId(), e.getMessage(), e);
        }
    }

    public void sendPaymentRequest(Payment payment) {

        Subscription actualSubscription = payment.getSubscription();
        // prepare payload to send after transaction commit
        Map<String, Object> req = new HashMap<>();
        req.put("paymentId", payment.getId() != null ? payment.getId().toString() : null);
        req.put("subscriptionId", actualSubscription.getId().toString());
        req.put("userId", actualSubscription.getUser().getId().toString());
        req.put("amount", payment.getAmount());
        req.put("attempt", payment.getAttemptCount());

        // register to publish after transaction commits to ensure DB changes are durable
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                paymentRequestProducer.sendPaymentRequest(req);
            }
        });
    }
}
