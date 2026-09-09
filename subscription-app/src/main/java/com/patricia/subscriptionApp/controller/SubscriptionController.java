package com.patricia.subscriptionApp.controller;

import com.patricia.subscriptionApp.exception.ResourceNotFoundException;
import com.patricia.subscriptionApp.entity.Subscription;
import com.patricia.subscriptionApp.dto.SubscriptionDto;
import com.patricia.subscriptionApp.dto.PageResponse;
import com.patricia.subscriptionApp.mapper.SubscriptionMapper;
import com.patricia.subscriptionApp.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Assinaturas", description = "Gerencia assinaturas. A renovação cria um novo ciclo para manter histórico por usuário. O endpoint de update existe como utilitário de desenvolvimento/teste para simular vencimento, renovação e cancelamento.")
public class SubscriptionController {

    private final SubscriptionService service;

    @Operation(summary = "Lista todas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    @GetMapping
    public ResponseEntity<List<SubscriptionDto>> getAll() {
        log.debug("GET /api/subscriptions - Retrieving all entities");
        List<SubscriptionDto> result = service.findAll().stream()
                .map(SubscriptionMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Lista paginada")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos")
    })
    @GetMapping("/paginated")
    public ResponseEntity<PageResponse<SubscriptionDto>> getAllPaginated(
            @Parameter(description = "Página", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Ordenação", example = "id") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Direção", example = "ASC") @RequestParam(defaultValue = "ASC") String sortDirection
    ) {
        log.debug("GET /api/subscriptions/paginated — page={}, size={}, sortBy={}, dir={}",
                page, size, sortBy, sortDirection);

        Page<Subscription> pageResult = service.findAllPaginated(page, size, sortBy, sortDirection);
        Page<SubscriptionDto> dtoPage = pageResult.map(SubscriptionMapper::toDto);
        PageResponse<SubscriptionDto> response = PageResponse.of(dtoPage);

        log.debug("Returning page {} with {} items", page, response.getContent().size());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Busca por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Não encontrada"),
            @ApiResponse(responseCode = "400", description = "ID inválido")
    })
    @GetMapping("/{uuid}")
    public ResponseEntity<SubscriptionDto> getById(
            @Parameter(description = "ID", required = true) @PathVariable UUID uuid
    ) {
        log.debug("GET /api/subscriptions/{} - Retrieving by id", uuid);
        SubscriptionDto dto = SubscriptionMapper.toDto(service.findById(uuid));
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Cria assinatura")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Criada"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "409", description = "Conflito")
    })
    @PostMapping
    public ResponseEntity<SubscriptionDto> create(
            @Parameter(description = "Dados", required = true) @Valid @RequestBody SubscriptionDto dto
    ) {
       log.info("Creating {}: {}", "subscriptions", dto);
        Subscription created = service.create(dto);
        log.info("Created Subscription with id={}", created.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(SubscriptionMapper.toDto(created));
    }

    @Operation(summary = "Cancela assinatura")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cancelada"),
            @ApiResponse(responseCode = "404", description = "Não encontrada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PutMapping("/{uuid}/cancel")
    public ResponseEntity<SubscriptionDto> cancel(
            @Parameter(description = "ID", required = true) @PathVariable UUID uuid
    ) {
       log.info("Cancel {} id={}", "subscription", uuid);
        Subscription updated = service.cancel(uuid);
        log.info("Canceled Subscription with id={}", updated.getId());
        return ResponseEntity.ok(SubscriptionMapper.toDto(updated));
    }

    @Operation(summary = "Histórico do usuário", description = "Retorna o histórico de ciclos de assinatura do usuário. A renovação cria um novo registro por ciclo para facilitar auditoria, depuração e demonstração do fluxo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK")
    })
    @GetMapping("/user/{userId}/history")
    public ResponseEntity<List<SubscriptionDto>> getUserSubscriptionHistory(
            @Parameter(description = "ID do usuário", required = true) @PathVariable UUID userId
    ) {
        log.debug("GET /api/subscriptions - Retrieving all entities from UserId={}", userId);
        List<SubscriptionDto> result = service.findAllByUserId(userId).stream()
                .map(SubscriptionMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Assinatura atual do usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Não encontrada")
    })
    @GetMapping("/user/{userId}/current")
    public ResponseEntity<SubscriptionDto> hasActiveSubscription(
            @Parameter(description = "ID do usuário", required = true) @PathVariable UUID userId
    ) {
        Subscription response = service.findCurrentSubscription(userId).orElseThrow(() -> new ResourceNotFoundException("Subscription", "userId", userId));
        return ResponseEntity.ok(SubscriptionMapper.toDto(response));
    }

    @Operation(
            summary = "Atualiza plano de assinatura",
            description = "A atualização pode ser feita informando apenas o userId (atualiza a assinatura ATIVA do usuário) ou informando userId + id da assinatura. A operação só é permitida para assinaturas com status ATIVA."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Atualizada"),
            @ApiResponse(responseCode = "404", description = "Não encontrada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PutMapping()
    public ResponseEntity<SubscriptionDto> update(
            @Parameter(description = "Dados", required = true) @Valid @RequestBody SubscriptionDto dto
    ) {
        log.info("Updating {} id={} with dto={}", "Subscriptions", dto.getId(), dto);
        Subscription updated = service.updatePlanType(dto);
        log.info("Updated Subscription with id={}", updated.getId());
        return ResponseEntity.ok(SubscriptionMapper.toDto(updated));
    }
}
