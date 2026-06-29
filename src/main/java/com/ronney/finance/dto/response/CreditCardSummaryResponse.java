package com.ronney.finance.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record CreditCardSummaryResponse(
        @Schema(
                description = "Card name",
                example = "Nubank Platinum",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String card,

        @Schema(
                description = "Credit limit",
                example = "10000.00",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        BigDecimal creditLimit,

        @Schema(
                description = "Used credit limit",
                example = "4200.00",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        BigDecimal usedLimit,

        @Schema(
                description = "Available credit limit",
                example = "5800.00",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        BigDecimal availableLimit,

        @Schema(
                description = "Open installments",
                example = "8",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        Integer openInstallments
) {
}
