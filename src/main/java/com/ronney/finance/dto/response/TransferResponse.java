package com.ronney.finance.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransferResponse(
        @Schema(
                description = "Transfer identifier",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        UUID id,

        @Schema(
                description = "Transfer description",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String description,

        @Schema(
                description = "Transfer amount",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        BigDecimal amount,

        @Schema(
                description = "Transfer date",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        LocalDate transactionDate,

        @Schema(
                description = "Source financial account identifier",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        UUID sourceAccountId,

        @Schema(
                description = "Source financial account name",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String sourceAccountName,

        @Schema(
                description = "Destination financial account identifier",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        UUID destinationAccountId,

        @Schema(
                description = "Destination financial account name",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String destinationAccountName
) {
}