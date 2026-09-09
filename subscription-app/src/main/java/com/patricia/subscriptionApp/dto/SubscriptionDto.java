package com.patricia.subscriptionApp.dto;

import com.patricia.subscriptionApp.enums.PlanType;
import com.patricia.subscriptionApp.enums.SubscriptionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;


@Schema(description = "Data Transfer Object for Subscription")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionDto {

    @Schema(description = "Id of the subscription", example = "Sample id", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private UUID id;

    @Schema(description = "Plano of the subscription", example = "Sample plano", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Plano cannot be null")
    private PlanType plano;

    @Schema(description = "Status of the subscription", example = "Sample status", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private SubscriptionStatus status;

    @Schema(description = "DataInicio of the subscription", example = "Sample dataInicio", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDate dataInicio;

    @Schema(description = "DataExpiracao of the subscription", example = "Sample dataExpiracao", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDate dataExpiracao;

    @Schema(description = "UserId of the User id for subscription", example = "Sample userId", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "UserId cannot be null")
    private UUID userId;

    @Override
    public String toString() {
        return "SubscriptionDto{" +
                "id='" + id + "'" + ", " +
                "plano='" + plano + "'" + ", " +
                "status='" + status + "'" + ", " +
                "dataInicio='" + dataInicio + "'" + ", " +
                "dataExpiracao='" + dataExpiracao + "'" + ", " +
                "userId='" + userId + "'" +
                "}";
    }

}
