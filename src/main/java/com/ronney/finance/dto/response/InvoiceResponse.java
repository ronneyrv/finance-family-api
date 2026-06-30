package com.ronney.finance.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

public record InvoiceResponse(

        @Schema(
                description = "Credit card name",
                example = "Nubank Platinum",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String card,

        @Schema(
                description = "Invoice closing day",
                example = "20",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        Integer closingDay,

        @Schema(
                description = "Invoice due day",
                example = "28",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        Integer dueDay,

        @Schema(
                description = "Invoice month",
                example = "10",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        Integer month,

        @Schema(
                description = "Invoice year",
                example = "2026",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        Integer year,

        @Schema(
                description = "Total invoice amount",
                example = "3000.00",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        BigDecimal total,

        @Schema(
                description = "Available credit limit after considering all open installments",
                example = "7000.00",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        BigDecimal availableLimit,

        @Schema(
                description = "Invoice installments",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        List<InvoiceInstallmentResponse> installments
) {
}
