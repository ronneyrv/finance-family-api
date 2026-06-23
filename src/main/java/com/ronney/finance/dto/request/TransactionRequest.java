package com.ronney.finance.dto.request;

import com.ronney.finance.domain.enums.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionRequest(
        @NotBlank
        String description,

        @NotNull
        @Positive
        BigDecimal amount,

        @NotNull
        LocalDate transactionDate,

        @NotNull
        TransactionType type,

        @NotNull
        UUID categoryId,

        UUID subCategoryId
) {
}
