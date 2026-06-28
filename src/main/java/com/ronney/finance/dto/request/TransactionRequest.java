package com.ronney.finance.dto.request;

import com.ronney.finance.domain.enums.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionRequest(
        @Schema(
                description = "Transaction description",
                example = "Salary June"
        )
        @NotBlank
        String description,

        @Schema(
                description = "Transaction amount",
                example = "8500.00"
        )
        @NotNull
        @Positive
        BigDecimal amount,

        @Schema(
                description = "Transaction date",
                example = "2026-06-30"
        )
        @NotNull
        LocalDate transactionDate,

        @Schema(
                description = "Transaction type",
                example = "INCOME"
        )
        @NotNull
        TransactionType type,

        @Schema(
                description = "Category identifier"
        )
        @NotNull
        UUID categoryId,

        @Schema(
                description = "Subcategory identifier"
        )
        UUID subCategoryId
) {
}
