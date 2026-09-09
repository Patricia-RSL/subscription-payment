package com.patricia.subscriptionApp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.UUID;

@Schema(description = "Data Transfer Object for User")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

    @Schema(description = "Id of the user", example = "Sample id", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private UUID id;

    @Schema(description = "Email of the user", example = "Sample email", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Email cannot be null")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "Email domain is invalid")
    private String email;


    @Override
    public String toString() {
        return "UserDto{" +
                "id='" + id + "'" + ", " +
                "email='" + email + "'" +
                "}";
    }

}
