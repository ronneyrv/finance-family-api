package com.ronney.finance.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InvoiceInstallmentResponse(
        @Schema(
                description = "Purchase description",
                example = "MacBook Pro M4"
        )
        String description,

        @Schema(
                description = "Installment",
                example = "3/12"
        )
        String installment,

        @Schema(
                description = "Installment amount",
                example = "110.00"
        )
        BigDecimal amount,

        @Schema(
                description = "Paid",
                example = "false"
        )
        Boolean paid,

        @Schema(
                description = "Payment date",
                example = "2026-10-28",
                nullable = true
        )
        LocalDate paidAt
) {
}
