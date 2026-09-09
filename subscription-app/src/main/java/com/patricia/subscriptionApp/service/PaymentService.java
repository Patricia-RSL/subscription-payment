package com.patricia.subscriptionApp.service;

import com.patricia.subscriptionApp.entity.Payment;
import com.patricia.subscriptionApp.entity.Subscription;
import org.springframework.transaction.annotation.Transactional;

public interface PaymentService {
    void requestPayment(Payment payment);

    void createNewPaymentAndSendRequest(Subscription actualSubscription);

    @Transactional
    void createPartialPaymentAndSendRequest(Subscription actualSubscription, Subscription newSubscription);
}
