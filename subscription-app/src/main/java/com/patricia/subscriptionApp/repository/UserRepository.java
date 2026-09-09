package com.patricia.subscriptionApp.repository;

import com.patricia.subscriptionApp.entity.User;
import jakarta.persistence.LockModeType;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    // Bloqueia o registro do usuário para que ninguém mais crie assinaturas para ele simultaneamente
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select u from User u where u.id = ?1")
    Optional<User> findByIdForUpdate(UUID id);

    boolean existsByEmail(@NotNull(message = "Email cannot be null") String email);
}