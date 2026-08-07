package com.ronney.finance.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record FinancialHealthResponse(

        @Schema(
                description = "Total household assets",
                example = "25000.00",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        BigDecimal totalAssets,

        @Schema(
                description = "Total household liabilities",
                example = "3200.00",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        BigDecimal totalLiabilities,

        @Schema(
                description = "Current household net worth",
                example = "21800.00",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        BigDecimal netWorth

) {
}