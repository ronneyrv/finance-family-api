package com.ronney.finance.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Month;
import java.util.List;

public record MonthlyCreditCardTrendResponse(

        @Schema(
                description = "Month"
        )
        Month month,

        @Schema(
                description = "Total invoice amount for all cards",
                example = "2850.90"
        )
        BigDecimal total,

        List<CreditCardMonthlyExpenseResponse> cards
) {
}