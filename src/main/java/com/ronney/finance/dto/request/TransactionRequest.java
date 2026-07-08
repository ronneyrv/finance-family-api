package com.ronney.finance.dto.request;

import com.ronney.finance.domain.enums.PaymentMethod;
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
                example = "INCOME",
                allowableValues = {
                        "INCOME",
                        "EXPENSE"
                }
        )
        @NotNull
        TransactionType type,

        @Schema(
                description = "Payment method used for the transaction",
                example = "PIX",
                allowableValues = {
                        "PIX",
                        "CASH",
                        "DEBIT_CARD",
                        "BANK_TRANSFER"
                }
        )
        PaymentMethod paymentMethod,

        @Schema(
                description = "Category identifier",
                example = "4d0df1d8-8b62-4c0e-bef8-7dbfb74b27f6"
        )
        @NotNull
        UUID categoryId,

        @Schema(
                description = "Subcategory identifier",
                example = "fd18d65e-87df-4a6e-aef5-d4b7fd0a2b8d"
        )
        UUID subCategoryId
) {
}
