package com.ronney.finance.dto.response;

import com.ronney.finance.domain.enums.FinancialHealthLevel;
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
        BigDecimal netWorth,

        @Schema(
                description = "Financial health score from 0 to 100",
                example = "87.50",
                accessMode = Schema.AccessMode.READ_ONLY
        )
                BigDecimal healthScore,

        @Schema(
                description = "Financial health classification",
                example = "EXCELLENT",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        FinancialHealthLevel healthLevel
) {
}