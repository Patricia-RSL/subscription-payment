package com.patricia.paymentSimulator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

// Swagger/OpenAPI
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/payment-simulator")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Simulador de Pagamentos", description = "Simula provedor externo para desenvolvimento e demonstração do desafio. Permite forçar resultados por pagamento e controlar o consumo da fila payment.request. Esses endpoints são utilitários de teste e não devem ficar expostos em produção.")
/*
 * Payment simulator controller:
 * - Forces the result (SUCCESS/FAILED) for a specific payment via endpoint, without relying on the queue.
 * - Turns on/off automatic consumption from the payment.request queue (listener "paymentListener").
 * - Exposes the current auto-consume state.
 *
 * Useful for development/testing: reproduces external provider scenarios without real infrastructure.
 */
public class SimulatorController {

    private final PaymentSimulatorService simulatorService;
    private final SimulatorState simulatorState;
    private final RabbitListenerEndpointRegistry registry;

    @Operation(summary = "Processar pagamento como sucesso", description = "Endpoint utilitário para desenvolvimento/teste. Publica manualmente um resultado de sucesso para o pagamento informado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Processado"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    @PostMapping("/process/{uuid}/success")
    public ResponseEntity<?> processSuccess(@Parameter(description = "ID do pagamento") @PathVariable UUID uuid) throws Exception {
        return simulatorService.processWithStatus(uuid, "SUCCESS");
    }

    @Operation(summary = "Processar pagamento como falha", description = "Endpoint utilitário para desenvolvimento/teste. Publica manualmente um resultado de falha para o pagamento informado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Processado"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    @PostMapping("/process/{uuid}/fail")
    public ResponseEntity<?> processFail(@Parameter(description = "ID do pagamento") @PathVariable UUID uuid) throws Exception {
        return simulatorService.processWithStatus(uuid, "FAILED");
    }

    @Operation(summary = "Processar como sucesso e forçar erro na publicação (aciona retry/DLQ)", description = "Endpoint utilitário para validar comportamento de retry e DLQ durante a demonstração do desafio.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Processado"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    @PostMapping("/process/{uuid}/force-error")
    public ResponseEntity<?> processForceError(@Parameter(description = "ID do pagamento") @PathVariable UUID uuid) throws Exception {
        return simulatorService.forceProcessWithError(uuid);
    }

    @Operation(summary = "Ligar auto-consume", description = "Reativa o consumo automático da fila de requisições de pagamento. Uso exclusivo em desenvolvimento/teste.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ligado"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    @PostMapping("/auto/on")
    public ResponseEntity<String> enableAutoConsume() {
        MessageListenerContainer container = registry.getListenerContainer("paymentListener");

        if (container == null) {
            return ResponseEntity.ok().body("Listener não encontrado");
        }
        simulatorState.setAutoConsume(true);
        container.start(); // Start consumer: Spring resumes reading the queue immediately

        return ResponseEntity.ok().body("Auto-consume ligado. Processando fila...");
    }

    @Operation(summary = "Desligar auto-consume", description = "Pausa o consumo automático da fila para simular indisponibilidade do provedor externo. Uso exclusivo em desenvolvimento/teste.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Desligado"),
        @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    @PostMapping("/auto/off")
    public ResponseEntity<?> disableAutoConsume() {
        MessageListenerContainer container = registry.getListenerContainer("paymentListener");

        if (container == null) {
            return ResponseEntity.ok().body("Listener não encontrado");
        }
        simulatorState.setAutoConsume(false);
        container.stop();
        return ResponseEntity.ok().body("Auto-consume desligado. Consumo pausado com segurança.");
    }

    @Operation(summary = "Consultar estado do auto-consume", description = "Retorna o estado atual do consumo automático do simulador. Uso exclusivo em desenvolvimento/teste.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado retornado")
    })
    @GetMapping("/auto")
    public ResponseEntity<?> getAutoConsume() {
        return ResponseEntity.ok(Map.of("autoConsume", simulatorState.isAutoConsume()));
    }
}
