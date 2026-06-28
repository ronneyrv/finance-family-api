package com.ronney.finance.dto.response;

import com.ronney.finance.domain.enums.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionResponse(
        @Schema(
                description = "Transaction identifier"
        )
        UUID id,

        @Schema(
                description = "Transaction description"
        )
        String description,

        @Schema(
                description = "Transaction amount"
        )
        BigDecimal amount,

        @Schema(
                description = "Transaction date"
        )
        LocalDate transactionDate,

        @Schema(
                description = "Transaction type"
        )
        TransactionType type,

        @Schema(
                description = "Transaction identifier category"
        )
        UUID categoryId,

        @Schema(
                description = "Transaction category"
        )
        String category,

        @Schema(
                description = "Transaction identifier subcategory"
        )
        UUID subCategoryId,

        @Schema(
                description = "Transaction subcategory"
        )
        String subCategory
) {
}
