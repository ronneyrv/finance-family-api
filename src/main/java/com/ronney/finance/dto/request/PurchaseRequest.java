package com.ronney.finance.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PurchaseRequest(
        @Schema(
                description = "Purchase description",
                example = "MacBook Pro M4"
        )
        @NotBlank
        String description,

        @Schema(
                description = "Purchase amount",
                example = "12000.00"
        )
        @NotNull
        @Positive
        BigDecimal totalAmount,

        @Schema(
                description = "Number of installments",
                example = "12"
        )
        @NotNull
        @Min(1)
        @Max(36)
        Integer installments,

        @Schema(
                description = "Purchase date",
                example = "2026-09-25"
        )
        @NotNull
        LocalDate purchaseDate
) {
}
