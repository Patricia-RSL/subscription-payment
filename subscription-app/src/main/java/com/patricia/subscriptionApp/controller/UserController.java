package com.patricia.subscriptionApp.controller;

import com.patricia.subscriptionApp.dto.PageResponse;
import com.patricia.subscriptionApp.dto.UserDto;
import com.patricia.subscriptionApp.entity.User;
import com.patricia.subscriptionApp.mapper.UserMapper;
import com.patricia.subscriptionApp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Usuários", description = "Operações de usuários: listar, paginação, buscar por ID, criar, atualizar e verificar existência")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @Operation(summary = "Listar todos os usuários", description = "Retorna todos os usuários como UserDto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    @GetMapping
    public ResponseEntity<List<UserDto>> getAll() {
        log.debug("GET /api/users - Retrieving all entities");
        List<UserDto> result = service.findAll().stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Listar usuários (paginado)", description = "Retorna uma página de usuários; suporta ordenação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos")
    })
    @GetMapping("/paginated")
    public ResponseEntity<PageResponse<UserDto>> getAllPaginated(
            @Parameter(description = "Número da página (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página (1–100)", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo de ordenação", example = "id") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Direção de ordenação (ASC/DESC)", example = "ASC") @RequestParam(defaultValue = "ASC") String sortDirection
    ) {
        log.debug("GET /api/users/paginated — page={}, size={}, sortBy={}, dir={}",
                page, size, sortBy, sortDirection);

        Page<User> pageResult = service.findAllPaginated(page, size, sortBy, sortDirection);
        Page<UserDto> dtoPage = pageResult.map(UserMapper::toDto);
        PageResponse<UserDto> response = PageResponse.of(dtoPage);

        log.debug("Returning page {} with {} items", page, response.getContent().size());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obter usuário por ID", description = "Retorna o usuário correspondente ao UUID informado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Não encontrado"),
            @ApiResponse(responseCode = "400", description = "ID inválido")
    })
    @GetMapping("/{uuid}")
    public ResponseEntity<UserDto> getById(
            @Parameter(description = "UUID do usuário", required = true) @PathVariable UUID uuid
    ) {
        log.debug("GET /api/users/{} - Retrieving by id", uuid);
        UserDto dto = UserMapper.toDto(service.findById(uuid));
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Criar usuário", description = "Cria e retorna o usuário criado")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Criado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "Conflito")
    })
    @PostMapping
    public ResponseEntity<UserDto> create(
            @Parameter(description = "Dados do usuário", required = true) @Valid @RequestBody UserDto dto
    ) {
       log.info("Creating {}: {}", "users", dto);
        User created = service.create(dto);
        log.info("Created User with id={}", created.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(UserMapper.toDto(created));
    }

    @Operation(summary = "Atualizar usuário", description = "Atualiza e retorna o usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Atualizado"),
            @ApiResponse(responseCode = "404", description = "Não encontrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PutMapping("/{uuid}")
    public ResponseEntity<UserDto> update(
            @Parameter(description = "UUID do usuário", required = true) @PathVariable UUID uuid,
            @Parameter(description = "Dados atualizados", required = true) @Valid @RequestBody UserDto dto
    ) {
       log.info("Updating {} id={} with dto= {}", "users", uuid, dto);
        User updated = service.update(uuid, dto);
        log.info("Updated User with id={}", updated.getId());
        return ResponseEntity.ok(UserMapper.toDto(updated));
    }

    @Operation(summary = "Verificar existência (HEAD)", description = "Retorna 200 se existir, 404 caso contrário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Existe"),
            @ApiResponse(responseCode = "404", description = "Não encontrado")
    })
    @RequestMapping(value = "/{uuid}", method = RequestMethod.HEAD)
    public ResponseEntity<Void> exists(
            @Parameter(description = "UUID do usuário", required = true) @PathVariable UUID uuid
    ) {
        log.debug("HEAD /api/users/{} - Checking existence", uuid);
        return service.existsById(uuid)
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

}
