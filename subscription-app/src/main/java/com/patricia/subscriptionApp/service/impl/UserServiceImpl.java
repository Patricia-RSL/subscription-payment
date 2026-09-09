package com.patricia.subscriptionApp.service.impl;

import com.patricia.subscriptionApp.dto.UserDto;
import com.patricia.subscriptionApp.entity.User;
import com.patricia.subscriptionApp.exception.BadRequestException;
import com.patricia.subscriptionApp.exception.EmailAlreadyInUseException;
import com.patricia.subscriptionApp.exception.ResourceNotFoundException;
import com.patricia.subscriptionApp.mapper.UserMapper;
import com.patricia.subscriptionApp.repository.UserRepository;
import com.patricia.subscriptionApp.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    /**
     * Returns all User entities (use paginated version for large datasets).
     */
    @Override
    public List<User> findAll() {
        log.debug("Finding all User entities");
        return repository.findAll();
    }

    /**
     * Returns a paginated, sorted page of User entities.
     */
    @Override
    public Page<User> findAllPaginated(int page, int size, String sortBy, String sortDirection) throws BadRequestException {
        log.debug("Paginating User — page={}, size={}, sort={} {}", page, size, sortBy, sortDirection);

        if (page < 0) throw new BadRequestException("Page number cannot be negative");
        if (size <= 0) throw new BadRequestException("Page size must be greater than 0");
        if (size > 100) {
            log.warn("Page size {} exceeds maximum, capping at 100", size);
            size = 100;
        }

        Sort.Direction direction = "DESC".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<User> result = repository.findAll(pageable);

        log.debug("Found {} of {} total User entities on page {}",
                result.getNumberOfElements(), result.getTotalElements(), result.getNumber());
        return result;
    }

    /**
     * Finds a single User by primary key.
     *
     * @throws ResourceNotFoundException when no record matches the given id
     */
    @Override
    public User findById(UUID id) throws BadRequestException {
        log.debug("Finding User by id={}", id);
        if (id == null) throw new BadRequestException("ID cannot be null");

        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    // ── Write ─────────────────────────────────────────────────────────

    /**
     * Creates a new User from the supplied DTO.
     *
     * <p>Relationship fields are resolved from their repositories
     * using the IDs provided in the DTO.
     */
    @Transactional
    @Override
    public User create(UserDto dto) throws BadRequestException {
        log.info("Creating User from dto={}", dto);
        if (dto == null) throw new BadRequestException("DTO cannot be null");

        if (repository.existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyInUseException("Email is already in use");
        }

        User user = UserMapper.toEntity(dto);

        User saved = repository.save(user);
        log.info("Created User id={}", saved.getId());
        return saved;
    }

    /**
     * Updates an existing User identified by {@code id} using the supplied DTO.
     *
     * <p>Relationship fields are re-resolved on every update call
     * so stale foreign-key references are never persisted.
     */
    @Transactional
    @Override
    public User update(UUID id, UserDto dto) throws BadRequestException {
        log.info("Updating User id={} with dto={}", id, dto);
        if (id == null)  throw new BadRequestException("ID cannot be null");
        if (dto == null) throw new BadRequestException("DTO cannot be null");


        User user = findById(id);
        UserMapper.updateEntity(user, dto);

        User updated = repository.save(user);
        log.info("Updated User id={}", updated.getId());
        return updated;
    }

    // ── Utility ───────────────────────────────────────────────────────

    @Override
    public boolean existsById(UUID id) {
        return id != null && repository.existsById(id);
    }

}
