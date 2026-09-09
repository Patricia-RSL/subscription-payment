package com.patricia.subscriptionApp.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a requested resource cannot be found in the data store.
 *
 * <p>Maps to HTTP {@code 404 Not Found}.
 *
 * <p>In multi-entity systems this is also thrown by service methods when
 * a related entity referenced by ID in the DTO does not exist.
 *
 * <p>Usage:
 * <pre>
 *   repository.findById(id)
 *       .orElseThrow(() -> new ResourceNotFoundException(entityName, "id", id));
 * </pre>
 */
@Getter
@ResponseStatus(HttpStatus.NOT_FOUND)
@Builder
@AllArgsConstructor
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    // Ensure a meaningful message is provided to exception handlers/clients
    @Override
    public String getMessage() {
        return String.format("%s not found with %s : '%s'", resourceName, fieldName, fieldValue);
    }
}
