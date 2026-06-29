package com.ronney.finance.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record CategoryExpenseResponse(
        @Schema(
                description = "Expense category name",
                example = "Groceries",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String category,

        @Schema(
                description = "Total amount spent in the category",
                example = "1350.50",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        BigDecimal amount
) {
}
