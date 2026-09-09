package com.patricia.subscriptionApp.entity;

import com.patricia.subscriptionApp.enums.PlanType;
import com.patricia.subscriptionApp.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Builder
@Getter
@Setter
@Entity
@Table(name = "subscriptions",
        indexes = {
                // Cobre: existsByUser_IdAndStatus, findByUser_IdAndStatus e findByUser_Id
                @Index(name = "idx_subscriptions_user_status", columnList = "user_id, status"),

                // Cobre: findByUser_IdOrderByDataExpiracaoDesc
                @Index(name = "idx_subscriptions_user_expiracao", columnList = "user_id, data_expiracao DESC"),

                // Cobre perfeitamente o Scheduler: findByStatusAndDataExpiracaoLessThanEqual
                @Index(name = "idx_subscriptions_status_expiracao", columnList = "status, data_expiracao")
}
)
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "plano", nullable = false)
    private PlanType plano;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SubscriptionStatus status;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_expiracao", nullable = false)
    private LocalDate dataExpiracao;

}