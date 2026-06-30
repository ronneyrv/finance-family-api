package com.ronney.finance.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

public record CreditCardResponse(
        @Schema(
                description = "Credit card identifier",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        UUID id,

        @Schema(
                description = "Credit card name",
                example = "Nubank Platinum",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String name,

        @Schema(
                description = "Credit limit",
                example = "10000.00",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        BigDecimal creditLimit,

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
        Integer dueDay
) {
}
