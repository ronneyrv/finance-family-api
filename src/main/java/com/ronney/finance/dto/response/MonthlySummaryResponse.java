package com.ronney.finance.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Month;

public record MonthlySummaryResponse(
        @Schema(
                description = "Month",
                example = "JANUARY"
        )
        Month month,

        @Schema(
                description = "Income",
                example = "8000.00"
        )
        BigDecimal income,

        @Schema(
                description = "Expense",
                example = "4200.00"
        )
        BigDecimal expense,

        @Schema(
                description = "Balance",
                example = "3800.00"
        )
        BigDecimal balance
) {
}
