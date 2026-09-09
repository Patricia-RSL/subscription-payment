package com.patricia.subscriptionApp.event;

import com.patricia.subscriptionApp.enums.PaymentStatus;
import lombok.Getter;

import java.util.UUID;

@Getter
public class PaymentProcessedEvent {
    private final UUID paymentId;
    private final PaymentStatus status;

    public PaymentProcessedEvent(UUID paymentId, PaymentStatus status) {
        this.paymentId = paymentId;
        this.status = status;
    }
}

