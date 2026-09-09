package com.patricia.subscriptionApp.repository;

import com.patricia.subscriptionApp.entity.Subscription;
import com.patricia.subscriptionApp.enums.SubscriptionStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    @Query("select (count(s) > 0) from Subscription s where s.user.id = ?1 and s.status = ?2")
    boolean existsByUser_IdAndStatus(UUID id, SubscriptionStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Subscription s where s.user.id = ?1 and s.status = ?2")
    Optional<Subscription> findByUser_IdAndStatusForUpdate(UUID userId, SubscriptionStatus status);

    List<Subscription> findByUser_Id(UUID usuarioId);

    @Query("select s from Subscription s where s.user.id = :userId and :refDate >= s.dataInicio and :refDate < s.dataExpiracao order by s.dataExpiracao desc")
    Optional<Subscription> findCurrentByUserIdAt(@Param("userId") UUID userId, @Param("refDate") LocalDate refDate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Subscription s where s.status = :status and s.dataExpiracao <= :dataExpiracao")
    List<Subscription> findByStatusAndDataExpiracaoLessThanEqualForUpdate(
            @Param("status") SubscriptionStatus status,
            @Param("dataExpiracao") LocalDate dataExpiracao
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Subscription s where s.id = :id")
    Optional<Subscription> findByIdForUpdate(@Param("id") UUID id);
}