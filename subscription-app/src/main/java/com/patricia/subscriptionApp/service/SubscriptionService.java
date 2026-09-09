package com.patricia.subscriptionApp.service;

import com.patricia.subscriptionApp.dto.SubscriptionDto;
import com.patricia.subscriptionApp.entity.Payment;
import com.patricia.subscriptionApp.entity.Subscription;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionService {
    Subscription suspendSubscription(Payment payment);

    List<Subscription> findAll();

    Page<Subscription> findAllPaginated(int page, int size, String sortBy, String sortDirection);

    Subscription findById(UUID id);

    @Transactional
    Subscription create(SubscriptionDto dto);

    @Transactional
    Subscription cancel(UUID id);

    List<Subscription> findAllByUserId(UUID userId);

    Optional<Subscription> findCurrentSubscription(UUID userId);

    Subscription updatePlanType(@Valid SubscriptionDto dto);
}
