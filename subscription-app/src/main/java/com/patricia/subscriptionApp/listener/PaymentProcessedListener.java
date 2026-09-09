package com.patricia.subscriptionApp.listener;

import com.patricia.subscriptionApp.event.PaymentProcessedEvent;
import com.patricia.subscriptionApp.entity.Payment;
import com.patricia.subscriptionApp.entity.Subscription;
import com.patricia.subscriptionApp.enums.PaymentStatus;
import com.patricia.subscriptionApp.enums.SubscriptionStatus;
import com.patricia.subscriptionApp.repository.PaymentRepository;
import com.patricia.subscriptionApp.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentProcessedListener {

    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;

    @EventListener
    @Transactional
    public void handle(PaymentProcessedEvent event) {

        log.info("Handling PaymentProcessedEvent for paymentId: {}, status: {}", event.getPaymentId(), event.getStatus());
        Optional<Payment> pOpt = paymentRepository.findById(event.getPaymentId());
        if (pOpt.isEmpty()) {
            log.warn("Payment not found: {}", event.getPaymentId());
            return;
        }

        Payment payment = pOpt.get();

        // Once sucessful payment is processed, we should not process it again
        // to avoid failing the subscription renewal process.
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            log.info("Payment already processed success: {}", payment.getId());
            return;
        }

        //If a payment is processed successfully, we should garantee that the subscription is activated
        if(event.getStatus() == PaymentStatus.SUCCESS) {
            payment.setStatus(event.getStatus());
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.saveAndFlush(payment);

            Subscription subscription = payment.getSubscription();
            subscription.setStatus(SubscriptionStatus.ATIVA);
            subscriptionRepository.saveAndFlush(subscription);
            log.info("Subscription renewed successfully: {}", subscription.getId());
        }
    }
}
