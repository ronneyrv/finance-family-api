package com.ronney.finance.dto.response;

import com.ronney.finance.domain.enums.PaymentMethod;
import com.ronney.finance.domain.enums.TransactionKind;
import com.ronney.finance.domain.enums.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionResponse(
        @Schema(
                description = "Transaction identifier",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        UUID id,

        @Schema(
                description = "Transaction description",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String description,

        @Schema(
                description = "Transaction amount",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        BigDecimal amount,

        @Schema(
                description = "Transaction date",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        LocalDate transactionDate,

        @Schema(
                description = "Transaction type",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        TransactionType type,

        @Schema(
                description = "Transaction kind",
                example = "REGULAR",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        TransactionKind transactionKind,

        @Schema(
                description = "Payment method used for the transaction",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        PaymentMethod paymentMethod,

        @Schema(
                description = "Financial account identifier",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        UUID accountId,

        @Schema(
                description = "Financial account name",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String accountName,

        @Schema(
                description = "Category identifier",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        UUID categoryId,

        @Schema(
                description = "Category name",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String category,

        @Schema(
                description = "Subcategory identifier",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        UUID subCategoryId,

        @Schema(
                description = "Subcategory name",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String subCategory
) {
}
