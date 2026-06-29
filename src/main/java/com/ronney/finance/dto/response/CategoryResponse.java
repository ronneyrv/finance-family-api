package com.ronney.finance.dto.response;

import com.ronney.finance.domain.enums.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record CategoryResponse(
        @Schema(
                description = "Category identifier"
        )
        UUID id,

        @Schema(
                description = "Category name",
                example = "Income"
        )
        String name,

        @Schema(
                description = "Transaction type",
                example = "INCOME"
        )
        TransactionType type
) {
}
