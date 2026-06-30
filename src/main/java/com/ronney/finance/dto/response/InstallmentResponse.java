package com.ronney.finance.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

public record InstallmentResponse(
        @Schema(
                description = "Installment identifier",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        UUID id,

        @Schema(
                description = "Purchase description",
                example = "MacBook Pro M4",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String description,

        @Schema(
                description = "Current installment number",
                example = "3",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        Integer installmentNumber,

        @Schema(
                description = "Total number of installments",
                example = "12",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        Integer totalInstallments,

        @Schema(
                description = "Installment amount",
                example = "1000.00",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        BigDecimal amount,

        @Schema(
                description = "Invoice month",
                example = "10",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        Integer invoiceMonth,

        @Schema(
                description = "Invoice year",
                example = "2026",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        Integer invoiceYear,

        @Schema(
                description = "Indicates whether the installment has been paid",
                example = "false",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        Boolean paid
) {
}
