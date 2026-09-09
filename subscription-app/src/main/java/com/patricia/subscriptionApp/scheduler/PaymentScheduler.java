package com.patricia.subscriptionApp.scheduler;

import com.patricia.subscriptionApp.entity.Payment;
import com.patricia.subscriptionApp.enums.PaymentStatus;
import com.patricia.subscriptionApp.repository.PaymentRepository;
import com.patricia.subscriptionApp.service.impl.PaymentServiceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentScheduler {

    private final PaymentRepository paymentRepository;
    private final PaymentServiceImpl paymentService;

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void requestPendingPayments() {

        log.info("Scheduled task started: Checking pending payments");
        List<Payment> pendingPayments = paymentRepository.findByStatusOrderByCreatedAtAsc(PaymentStatus.PENDING);
        log.info("Found {} pending payments to process", pendingPayments.size());

        for(Payment payment: pendingPayments){

            paymentService.requestPayment(payment);

        }
    }

}
