package com.ronney.finance.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Month;

public record CashFlowResponse(

        @Schema(
                description = "Reference month",
                example = "JANUARY",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        Month month,

        @Schema(
                description = "Total household income",
                example = "12500.00",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        BigDecimal income,

        @Schema(
                description = "Total household expenses",
                example = "8300.00",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        BigDecimal expense

) {
}