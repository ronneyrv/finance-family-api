package com.ronney.finance.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Month;

public record CumulativeResultResponse(

        @Schema(
                description = "Reference month",
                example = "JANUARY",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        Month month,

        @Schema(
                description = "Total household income",
                example = "12000.00",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        BigDecimal income,

        @Schema(
                description = "Total household expenses",
                example = "8000.00",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        BigDecimal expense,

        @Schema(
                description = "Monthly financial result",
                example = "4000.00",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        BigDecimal monthlyResult,

        @Schema(
                description = "Accumulated financial result",
                example = "15000.00",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        BigDecimal accumulatedResult

) {
}