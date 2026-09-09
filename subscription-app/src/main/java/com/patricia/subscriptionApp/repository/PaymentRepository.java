package com.patricia.subscriptionApp.repository;

import com.patricia.subscriptionApp.entity.Payment;
import com.patricia.subscriptionApp.enums.PaymentStatus;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "10000")})
    List<Payment> findByStatusOrderByCreatedAtAsc(PaymentStatus status);

}
