package com.ronney.finance.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @Schema(
                description = "User email",
                example = "demo@finance-api.com"
        )
        @Email
        @NotBlank
        String email,

        @Schema(
                description = "User password",
                example = "demo123"
        )
        @NotBlank
        String password
) {
}