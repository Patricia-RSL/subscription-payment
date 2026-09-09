package com.patricia.subscriptionApp.controller;

import com.patricia.subscriptionApp.scheduler.CancelPendingScheduler;
import com.patricia.subscriptionApp.scheduler.PaymentScheduler;
import com.patricia.subscriptionApp.scheduler.RenewScheduler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Jobs", description = "Aciona rotinas agendadas manualmente. Endpoints administrativos expostos apenas para desenvolvimento, testes e demonstração do desafio; não devem permanecer públicos em produção.")
public class JobController {

    private final RenewScheduler renewScheduler;
    private final CancelPendingScheduler cancelPendingScheduler;
    private final PaymentScheduler paymentScheduler;

    @Operation(summary = "Executa renovação", description = "Dispara manualmente a rotina agendada de renovação para facilitar testes e demonstrações sem depender do horário real do cron.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Executado"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    @PostMapping("/renew")
    public void renew( ) {
        renewScheduler.renewAllActiveSubscriptions();
    }

    @Operation(summary = "Finaliza cancelamentos pendentes", description = "Dispara manualmente a rotina que converte assinaturas canceladas pendentes em canceladas definitivas quando o ciclo termina.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Executado"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    @PostMapping("/cancel-pending")
    public void cancelPending( ) {
        cancelPendingScheduler.finalizePendingCancellations();
    }

    @Operation(summary = "Verifica pagamentos pendentes", description = "Dispara manualmente a rotina que reenfileira pagamentos pendentes até o limite de 3 tentativas. Utilitário de teste/desenvolvimento.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Executado"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    @PostMapping("/pending-payments")
    public void checkPendingPayments( ) {
        paymentScheduler.requestPendingPayments();
    }
}
