package com.ronney.finance.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record DashboardSummaryResponse(
        @Schema(
                description = "Total income",
                example = "12000.00",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        BigDecimal totalIncome,

        @Schema(
                description = "Total expenses",
                example = "5400.50",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        BigDecimal totalExpense,

        @Schema(
                description = "Current balance",
                example = "6599.50",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        BigDecimal balance
) {
}
