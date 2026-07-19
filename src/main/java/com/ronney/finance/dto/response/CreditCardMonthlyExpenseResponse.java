package com.ronney.finance.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record CreditCardMonthlyExpenseResponse(

        @Schema(
                description = "Credit card name",
                example = "Nubank"
        )
        String cardName,

        @Schema(
                description = "Invoice amount for the month",
                example = "1250.75"
        )
        BigDecimal amount
) {
}